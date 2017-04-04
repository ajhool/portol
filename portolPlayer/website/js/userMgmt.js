 /***********************************************************
  * Let's you listen to a value change
  ***********************************************************/
 if (!Object.prototype.watch) {
		Object.defineProperty(Object.prototype, "watch", {
			  enumerable: false,
			 configurable: true,
			 writable: false,
			 value: function (prop, handler) {
				var
				  oldval = this[prop],
				 getter = function () {
					return oldval;
				},
				 setter = function (newval) {
					if (oldval !== newval) {
						handler.call(this, prop, oldval, newval);
						oldval = newval;
					}
					else { return false; }
				}
				;
				
				if (delete this[prop]) { // can't watch constants
					Object.defineProperty(this, prop, {
						get: getter,
						set: setter,
						enumerable: true,
						configurable: true
					});
				}
			}
		});
	}


/*
 * Google
 */
var googleToken = null;
var googleEmail = null;
var googleName = '';
var facebookToken = null;

var gVueVendor;
var gFBVendor;
var gGoogleVendor;
var gWhoAmICycle;
 
 /*
  * 1. WAIT FOR VUESCAPE !IMPORTANT
  * 2. Vendors: for_waiting(), add callbacks for when it changes to success to check for if others waiting
  * 3. Vendors: if none are still waiting, blink out.
  *
  */

var VSAuthAdapter = function(type, caller){
	var authTypes = {google: 'GOOGLE', fb: 'FACEBOOK'};
	
	if(!authTypes[type]){
		console.log('ID vendor not supported: ' + type + '. Available: ', authTypes);
		return;
	}
	var mType = authTypes[type];
	
	var Message = function(token){
		//We won't know loggingIn or loginPlatform for social vendors, so ignore.
		this.loggingIn = null;
		this.loginPlatform = null;
		this.type = mType;
		this.oAuthToken = token;
	};
	/*
	var User = function(attrs){
		this.hashedPass = attrs.password;
		this.firstName = googleName;
		this.email = attrs.email;
		this.lastName = null;
		this.signUpDate = null;
		this.lastSeen = null;
		this.userShards = null;
		this.history = null;
		this.userName = attrs.userName;
		return this;
	};
	*/
	this.send = function(token) {	  
		var self = this;
   		var message = new Message(token);
   		var messageString = JSON.stringify(message);

	   	var r = new XMLHttpRequest();
	   	//loginOrRegister is for social only
	   	//make this social login
	   	var to = "https://www.portol.me:5555/api/v0/user/loginorregister";
	   	r.open("POST", to, true);
	   	r.onreadystatechange = function () {
			 if (r.readyState != 4 || r.status != 200) {
			 	caller.setStatus('VS_DENIED');
				return;
			}
			
			caller.setStatus('VS_CONFIRMED');
	   	};
	   
	   r.setRequestHeader('Content-Type','application/json');
	   r.send(messageString);
	   
	   return this;
	};
	
	this.reply = function(){
		
	};
};

var FBVendor = function(){
	var self = this;
	
	//console.log('facebook', FB.api);
	
	this.vsAuth = new VSAuthAdapter('fb', this);
	this.oAuthToken = null;
	
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
			case veriStatus.
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

var VueVendor = function(){
	var self = this;
	var veriStatus = {
		waiting: 'WAITING',
		confirmed: 'CONFIRMED',
		notConnected: 'NOT_CONNECTED',
		denied: 'DENIED',
		vsConfirmed: 'VS_CONFIRMED',
		vsDenied: 'VS_DENIED'
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
			console.log('Tried to set unknown status');
		}
	};
	
	this.status = veriStatus.waiting;
	
	this.whoAmI = function(){
		/*
		   var user = new mUserMgmt.User({
			'userName':userName,
			'password': hashed
			});
		*/

	   var r = new XMLHttpRequest();
	   //loginOrRegister is for social only
	   var to = "https://www.portol.me:5555/api/v0/user/info/me";
	   r.open("GET", to, true);
	   r.onreadystatechange = function(response) {
			switch(r.status) {
				case 200:
					//User found on this platform
					self.status = veriStatus.confirmed;
					break;
				case 402:
					//No user found on this platform, keep rolling.
					self.status = veriStatus.denied;
					break;
				default:
					console.log('Unhandled response type for whoAmI call');
					break;
			}
	   };
	   r.send();
	};
	
	this.processStatusUpdate = function(oldVal, newVal){
		if(newVal == veriStatus.confirmed) {
			
		}
	};
};

/*
 * Used for silent sign in on page load.
 */
var WhoAmICycle = function(vueScape, socialVendors){
	var self = this;
	var mIsVSVerified = null;
	var idVendors = ['vuescape', 'facebook', 'google'];
	
	//Waiting, Confirmed, Unconfirmed
	this.decision = 'WAITING';
	
	this.updateDecision = function(value){
		if(value === 'CONFIRMED'){
			this.decision = 'CONFIRMED';
			alert('Decision made, logged in.');
		} else if (value === 'DENIED') {
			this.decison = 'DENIED';
			alert('Decision made, logged out.');
		};
	};
	
	//Vuescape can update this parameter, it means vuescape knows the user.
	var iAm = null;
	
	this.vueScape = vueScape;	
	this.fbVendor = socialVendors.fb;
	this.googleVendor = socialVendors.google;
	
	this.isWaitingOnAnybody = function(newGuy){
		var isWaiting = true;
		
		//This is a bit of a hack due to the wait the .watch() function works. It does not update the .status
		// value before making the listener's callbacks. Ideally, we would just check all three vendor's statuses
		// in this function. But because it is being called on status changes of "newGuy", newGuy's status hasn't
		// updated yet, so we have to assume it is 'DENIED';
		switch(newGuy){
			case 'fb':
				isWaiting = !('DENIED' === this.vueScape.status && 'VS_DENIED' === this.googleVendor.status); 
				break;
			case 'vs':
				isWaiting = !('VS_DENIED' === this.fbVendor.status && 'VS_DENIED' === this.googleVendor.status);
				break;
			case 'google':
				isWaiting = !('DENIED' === this.vueScape.status && 'VS_DENIED' === this.fbVendor.status);
				break;
			default:
				isWaiting = !('DENIED' === this.vueScape.status && 
				'VS_DENIED' === this.fbVendor.status && 
				'VS_DENIED' === this.googleVendor.status);
		}
		/*
		if(('DENIED' === this.vueScape.status && 
				'VS_DENIED' === this.fbVendor.status && 
				'VS_DENIED' === this.googleVendor.status)){
			isWaiting = false;
			console.log('Done waiting, no users seen.');
		} else {
			console.log('Still checking users.');
			isWaiting = true;
		}*/
		
		if(isWaiting){
			console.log('Is still waiting on id vendor');
		} else {
			console.log('Is not waiting on id vendor.');
		}
		
		return isWaiting;
	};
	
	this.vueScape.watch('status', function(field,oldVal,newVal){
		if('CONFIRMED' == newVal) {
			self.updateDecision('CONFIRMED');
		} else if('DENIED' === newVal){
			if(!self.isWaitingOnAnybody('vs')){
				self.updateDecision('DENIED');
			}
		}
	});
	
	this.fbVendor.watch('status', function(field,oldVal,newVal){
		if(self.decision !== 'CONFIRMED' && self.decision !== 'VS_CONFIRMED') {
			//if(newVal == 'VS_CONFIRMED'){
			//	self.decision = 'CONFIRMED';
			//	console.log('VS Confirmed with facebook.');
			//} else {
			//	this.processStatusUpdate(oldVal, newVal);
			//}
			switch(newVal){
				case 'VS_CONFIRMED':
					self.updateDecision('CONFIRMED');
					console.log('VS Confirmed with facebook.');
					break;
				case 'VS_DENIED':
					if(!self.isWaitingOnAnybody('fb')){
						self.updateDecision('DENIED');
					}
					break;
				default:
					this.processStatusUpdate(oldVal, newVal);
					break;
			}
			
		}
	});
	
	this.googleVendor.watch('status', function(field,oldVal,newVal){
		if(self.decision !== 'CONFIRMED' && self.decision !== 'VS_CONFIRMED') {			
			switch(newVal){
				case 'VS_CONFIRMED':
					self.updateDecision('CONFIRMED');
					break;
				case 'VS_DENIED':
					if(!self.isWaitingOnAnybody('google')){
						self.updateDecision('DENIED');
					}
					break;
				default:
					this.processStatusUpdate(oldVal, newVal);
					break;
			}
		}
	});
	
	this.runCycle = function(){
		this.vueScape.whoAmI();
		//this.fbVendor.whoAmI();
		//this.googleVendor.whoAmI();
	};
	
	return this;
};

/**
 * User management
 */
var UserMgmt = function(){
	var mUserMgmt = this;
	var authTypes = {google: 'GOOGLE', fb: 'FACEBOOK', vuescape: 'VUESCAPE'};
	
	this.Message = function(user, platform, type){
		this.loggingIn = user;
		this.loginPlatform = platform;
		this.type = type;
		this.oAuthToken = facebookToken || googleToken ||  null;
	};
	
	this.User = function(attrs){
		this.hashedPass = attrs.password;
		this.firstName = googleName;
		this.email = attrs.email;
		this.lastName = null;
		this.signUpDate = null;
		this.lastSeen = null;
		this.userShards = null;
		this.history = null;
		this.userName = attrs.userName;
		return this;
	};
	
	this.login = function() {
	   var usernameField = document.getElementById('username-field');
	   var userName = usernameField.value || null;
	   var passwordField = document.getElementById('password-field');
	   var password = passwordField.value || null;
	   
	   var idToken = googleToken;
	   
	   var hashed = null;
	   if(null !== password){
			try {
				hashed = CryptoJS.MD5(password);
			} catch(e) {
				console.log("password hashing didn't work");
			}
   		}
   		
	   var user = new mUserMgmt.User({
	   			'userName':userName,
	   		  	'password': hashed
	   		  	});

	   var r = new XMLHttpRequest();
	   
	   //loginOrRegister is for social only
	   var to = "https://www.portol.me:5555/api/v0/user/loginorregister";
	   r.open("POST", to, true);
	   r.onreadystatechange = function () {
			 if (r.readyState != 4 || r.status != 200) {
				return;
			}
	   };
	   
	   r.setRequestHeader('Content-Type','application/json');
	   var platform = null;
		
	   var message = new mUserMgmt.Message(user, platform, authTypes.fb);

	   r.send(JSON.stringify(message));
	   
	   return this;
	};

	this.register = function() {
	   var usernameField = document.getElementById('rUsernameField');
	   var userName = usernameField.value;
	   var passwordField = document.getElementById('rPasswordField');
	   var password = passwordField.value;
	   var emailField = document.getElementById('rEmailField');
	   var email = emailField.value;
   
	   var user = new mUserMgmt.User(userName, password);
	   user.email = email;
   
	   var r = new XMLHttpRequest();
	   r.open("POST", "https://www.portol.me:5555/api/v0/user/newuser", true);
	   r.onreadystatechange = function () {
		 if (r.readyState != 4 || r.status != 200) return;
		 console.log(r);
	   };
	   r.setRequestHeader('Content-Type','application/json');
   
	   r.send(JSON.stringify(user));
	};
	
	return this;
};

var UserMgmtView = function(){
	var mMgmt;
	var sMgmt;
	this.getFields = function(){
		var username = 'username-field';
		var password = 'password-field';
		var passwordConfirm = 'password-confirm-field';
		var email = 'email-field';
	
		this.fields = {};
		this.fields.username = document.getElementById(username);
		this.fields.password = document.getElementById(password);
		this.fields.passwordConfirm = document.getElementById(passwordConfirm);
		this.fields.email = document.getElementById(email);
		
		return this;
	};

	this.getElements = function(){
		var loginClass = 'login';
		var registerClass = 'register';
		
		this.elements = {};
		this.elements.allRegister = document.getElementsByClassName(registerClass);
		this.elements.allLogin = document.getElementsByClassName(loginClass);
		
		return this;
	};

	this.getButtons = function(){
		var login = 'login-button';
		var register = 'register-button';
		var logout = 'logout-button';
		var loginSwitcher = 'login-switcher';
		var registerSwitcher = 'register-switcher';
		
		this.buttons = {};
		
		this.buttons.login = document.getElementById(login);
		this.buttons.login.addEventListener('click', function(){
			mMgmt.login();
		});
		
		this.buttons.register = document.getElementById(register);
		this.buttons.register.addEventListener('click', function(){
			mMgmt.register();
		});
		
		this.buttons.logout = document.getElementById(logout);
		this.buttons.logout.addEventListener('click', function(){
			console.log(sMgmt);
			sMgmt.logout();
		});
		
		this.loginSwitcher = document.getElementById(loginSwitcher);
		this.loginSwitcher.addEventListener('click', this.display.bind(this, 'LOGIN'));
		
		this.registerSwitcher = document.getElementById(registerSwitcher);
		this.registerSwitcher.addEventListener('click', this.display.bind(this, 'REGISTER'));
	
		return this;
	};
	
	this.build = function(mgmt, _smgmt){
		mMgmt = mgmt;
		sMgmt = _smgmt;
		console.log(mMgmt);
		this.getFields();
		this.getElements();
		this.getButtons();
	};

	this.display = function(toDisplay){
		var toShow = null;
		var toHide = null;
		
		switch(toDisplay) {
			case 'REGISTER':
				toShow = this.elements.allRegister;
				toHide = this.elements.allLogin;
				break;
			case 'LOGIN':
				toShow = this.elements.allLogin;
				toHide = this.elements.allRegister;
				break;
			default:
				toShow = this.elements.allLogin;
				toHide = this.elements.allRegister;
		}
		
		for(var showCtr=0; showCtr<toShow.length; showCtr++){
			toShow[showCtr].classList.remove('is-removed');
		}
		
		for(var hideCtr=0; hideCtr<toHide.length; hideCtr++){
			toHide[hideCtr].classList.add('is-removed');
		}
	};
};

var VS_AuthInterface = function(){
	var vue = new VueVendor();
	var fbVendor = new FBVendor();
	var googleVendor = new GoogleVendor();
	
	var whoAmICycle = new WhoAmICycle(vue, {'google': googleVendor, 'fb': fbVendor});
	
	this.start = function(){
		whoAmICycle.runCycle();
	};
	
	this.cycle = whoAmICycle;
	this.v = vue;
	this.f = fbVendor;
	this.g = googleVendor;
	
	return this;
};

/*
 * This should really block on the cuntly god damn fucking website.
 */
var vs_authInterface = new VS_AuthInterface();
vs_authInterface.start();