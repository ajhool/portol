function EndpointDeleteUtils(version){
	this.VERSION = version || "v0";
	
	this.currentPayment = 0;
	
	this.LOAD_BAL_URL = "https://www.portol.me";
	this.WILDCARD_URL = "https://wildcard.portol.com";
	
	this.LOAD_BAL_PORT = ":8443";
	this.BUY_PORT = ":5555";
	this.WILDCARD_PORT = ":5555";
}

EndpointDeleteUtils.prototype.buildUrl = function(type, params) {	
	var baseUrl = "";
	var port = "";
	var path = "/";
	var dest = type;
	
	//USUALLY dest and type will be logically the same, but not always.

	var invalidType = false;
	switch(type) {
		case "bookmark":
			var user = params.loggedIn || "";
			var videoKey = params.videoKey || "";
			
			baseUrl = this.LOAD_BAL_URL;
			port = this.BUY_PORT;
			dest = "bookmark";
			path = "/api/" + this.VERSION + "/" + dest + "/" + user.id + "/" + videoKey + "/";
			break;
		default:
			console.log("API destination is not supported: " + type + ".");
			invalidType = true;
	}
	
	var target;
	if(!invalidType) {
		target = baseUrl + port + path;
	} else {
		target = undefined;
	}
	
	return target;
};

