var FBVendor = function(){
	var self = this;
	
	//console.log('facebook', FB.api);
	
	this.vsAuth = new VSAuthAdapter('fb', this);
	this.oAuthToken = null;
	
	this.initialize = function(){
		//It's possible that the Facebook button has returned before this code can execute
		// In that case, don't wait for the callback.. just fire away.
		if(!window.FBPromise.hasReturned){
			window.FBPromise.callback = function(){
				self.whoAmI();
			};
		} else {
			self.whoAmI();
		}
		window.FBPromise.hasListener = true;
	};
	

	var veriStatus = {
		waiting: 'WAITING',
		confirmed: 'CONFIRMED',
		notConnected: 'NOT_CONNECTED',
		denied: 'DENIED',
		vsConfirmed: 'VS_CONFIRMED',
		vsDenied: 'VS_DENIED'
	};
	
	this.status = veriStatus.waiting;
	
	this.setStatus = function(status){
		if(status == veriStatus.waiting ||
			status == veriStatus.confirmed ||
			status == veriStatus.denied ||
			status == veriStatus.notConnected ||
			status == veriStatus.vsConfirmed ||
			status == veriStatus.vsDenied) {
			
			this.status = status;
		} else {
			console.log('Invalid status');
		}
	};
	
	//whoAmI call (FB vendor only) is not driving a network call. It is merely polling the value
	// acquired by the FB button, asynchronously.
	this.whoAmI = function(){
		FB.getLoginStatus(function(response) {
			console.log('Facebook get login status response: ', response);
			self.updateState(response);
		}); 
	};
	
	//updateState parses out the response
	this.updateState = function(response){
		
		// The response object is returned with a status field that lets the
		// app know the current login status of the person.
		// Full docs on the response object can be found in the documentation
		// for FB.getLoginStatus().
		/* Response object
		{
			status: 'connected',
			authResponse: {
				accessToken: '...',
				expiresIn:'...',
				signedRequest:'...',
				userID:'...'
			}
		}
		*/
		if (response.status === 'connected') {
		  // Logged into your app and Facebook.
		  //self.showAccountInfo();
		  this.oAuthToken = response.authResponse.accessToken;
		  this.setStatus('CONFIRMED');
		} else if (response.status === 'not_authorized') {
		  // The person is logged into Facebook, but not your app.
		  // User is not logged in
		  this.setStatus('VS_DENIED');
		  console.log('Logged into facebook, but needs to register for my app');
		} else {
		  // The person is not logged into Facebook, so we're not sure if
		  // they are logged into this app or not.
		  //document.getElementById('status').innerHTML = 'Please log ' +
			//'into Facebook.';
			this.setStatus('VS_DENIED');
			console.log('Logged into neither my app nor facebook');
		}
	};
	
	this.getInfo = function(){
		FB.api('/me?fields=name,picture,email', function(response) {
			console.log(response);	
			//document.getElementById('accountInfo').innerHTML = ('<img src="' + response.picture.data.url + '"> ' + response.name + ' ' + response.email);
		});
	};
	
	this.processStatusUpdate = function(oldVal, newVal){
		if(newVal === veriStatus.confirmed) {
			console.log('facebook should get from vs');
			this.getFromVuescape();
		} else if(newVal === veriStatus.denied) {
			console.log('facebook is not confirmed, dude');
		}
	};
	
	this.getFromVuescape = function(){
		if(!this.oAuthToken) {
			return;
		}
		this.vsAuth.send(this.oAuthToken);
	};
	
	this.logout = function(){
		FB.logout(function(response) {
 			console.log('User is logged out of facebook.'); // user is now logged out
		});
	};

	return this;
};