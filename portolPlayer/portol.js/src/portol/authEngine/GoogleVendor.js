var GoogleVendor = function(){
	/*
	window.onSignIn = function(googleUser) {
		var profile = googleUser.getBasicProfile();
		console.log('ID: ' + profile.getId()); // Do not send to your backend! Use an ID token instead.
		console.log('Name: ' + profile.getName());
		console.log('Image URL: ' + profile.getImageUrl());
		console.log('Email: ' + profile.getEmail());
		googleName = profile.getName();  
		googleEmail = profile.getEmail();
		googleAuthToken = googleUser.getAuthResponse().id_token;
		gGoogleVendor.setStatus('CONFIRMED');

		return this;
	};
	*/
	var self = this;
	
	var veriStatus = {
		waiting: 'WAITING',
		confirmed: 'CONFIRMED',
		notConnected: 'NOT_CONNECTED',
		denied: 'DENIED',
		vsConfirmed: 'VS_CONFIRMED',
		vsDenied: 'VS_DENIED'
	};
	
	this.oAuthToken = null;
	this.vsAuth = new VSAuthAdapter('google', this);
	
	this.status = veriStatus.waiting;
	
	this.initialize = function(){
		//check if google has already responded. If not, pipe the callback directly here.
		if(!window.googleUser){
			window.onGoogleSignIn = function(googleUser){
				console.log('on google sign in');
				window.googleUser = googleUser;
				self.currentUser = googleUser;
				self.oAuthToken = googleUser.getAuthResponse().id_token;
				self.setStatus('CONFIRMED');
			};
			
			window.onVSGoogleLoad = function(){
				self.auth2 = gapi.auth2.getAuthInstance();
				
				self.auth2.then(function(){
					self.whoAmI();
				});
				//console.log(auth2);
				//self.auth2.currentUser.get();
				console.log('is signed in:', self.auth2.isSignedIn.get());
			};
		} else {
			console.log('Google responded before GoogleVendor code was setup. Should still work.');
			this.oAuthToken = window.googleUser.getAuthResponse().id_token;
			this.setStatus('CONFIRMED');
		}
	};
	//var auth2 = gapi.auth2.getAuthInstance();
	//console.log(auth2);
    ///auth2..then(function () {
    //  console.log('User signed out.');
    //});


	this.whoAmI = function(){
		//already happening asynchronously in the background.
		if(this.auth2.isSignedIn.get()){
			this.currentUser = this.auth2.currentUser.get();
			this.oAuthToken = this.currentUser.getAuthResponse().id_token;
			self.setStatus('CONFIRMED');
		} else {
			self.setStatus('VS_DENIED');
		}
	};
	
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
	
	this.processStatusUpdate = function(oldVal, newVal){
		//if(newVal === veriStatus.confirmed) {
		//	this.getFromVuescape();
		//}
		switch(newVal) {
			case veriStatus.confirmed:
				this.getFromVuescape();
				break;		
			case veriStatus.notConnected:
				//THIS USER DOES NOT HAVE A VUESCAPE ACCOUNT THROUGH Google
				
				break;
			default:
				console.log('Unhandled status from google vendor process: ' + newVal + '.');
		}
	};
	
	this.getFromVuescape = function(){
		if(!this.oAuthToken) {
			console.log('Google expected to have an oAuthToken but did not.');
			return;
		}
		this.vsAuth.send(this.oAuthToken);
	};
	
	return this;
};