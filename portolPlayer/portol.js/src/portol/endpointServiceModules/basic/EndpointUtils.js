function EndpointUtils(version){
	this.VERSION = version || "v0";
	
	this.currentPayment = 0;
	
	this.LOAD_BAL_URL = "https://www.portol.me";
	this.WILDCARD_URL = "https://wildcard.portol.com";
	
	this.LOAD_BAL_PORT = ":8443";
	this.BUY_PORT = ":5555";
	this.WILDCARD_PORT = ":5555";
}

EndpointUtils.prototype.buildUrl = function(type) {	
	var baseUrl = "";
	var port = "";
	var path = "/";
	
	//USUALLY dest and type will be logically the same, but not always.
	var dest = type;
		
	var invalidType = false;
	switch(type) {
		case "init":
			baseUrl = this.LOAD_BAL_URL;
			port = this.LOAD_BAL_PORT;
			path = "/api/" + this.VERSION + "/";
			break;
		case "start":
			baseUrl = this.LOAD_BAL_URL;
			port =  this.LOAD_BAL_PORT;
			path = "/api/" + this.VERSION + "/";
			break;
		case "preview":
			baseUrl = this.LOAD_BAL_URL;
			port = this.LOAD_BAL_PORT;
			path ="/api/" + this.VERSION + "/";
			break;
		case "playerBuy":
			baseUrl = this.LOAD_BAL_URL;
			port = this.BUY_PORT;
			path = "/api/" + this.VERSION + "/";
			dest = "buyvideo/embedded";
			break;
		case "bookmark":
			baseUrl = this.LOAD_BAL_URL;
			port = this.BUY_PORT;
			path = "/api/" + this.VERSION + "/";
			break;
		case "user":
			baseUrl = this.LOAD_BAL_URL;
			port = this.BUY_PORT;
			path = "/api/" + this.VERSION + "/user/info/platform";
			dest = "";
			break;
		default:
			console.log("API destination is not supported: " + type + ".");
			invalidType = true;
	}
	
	var target;
	if(!invalidType) {
		target = baseUrl + port + path + dest;
	} else {
		target = undefined;
	}
	
	return target;
};

