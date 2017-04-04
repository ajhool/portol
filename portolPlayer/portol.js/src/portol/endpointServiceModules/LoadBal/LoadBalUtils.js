function LoadBalUtils(baseUrl, version){
	this.baseUrl = baseUrl;
	this.version = version;
	
	this.currentPayment = 0;
}

LoadBalUtils.prototype.checkPayment = function(response){
	var isPaid = false;
	
	if(response.mpdAuthorized) {
		isPaid = true;
	}
	
	return isPaid;
};
//TODO: This will not recurse
LoadBalUtils.prototype.startPolling = function(){
	var self = this;
	if(this.checkPayment()){
		this.triggerInitReady();
	} else {
		this.startQuery = setTimeout(function(){self.startPolling();}, 3000);
	}
};

LoadBalUtils.prototype.buildUrl = function(extension) {
	return this.baseUrl + "/" + this.version + "/" + extension;
};

