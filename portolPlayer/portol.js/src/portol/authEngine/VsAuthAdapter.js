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