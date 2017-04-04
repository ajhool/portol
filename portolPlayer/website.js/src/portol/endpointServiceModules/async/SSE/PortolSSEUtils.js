function PortolSSEUtils(){
	this.apiUriExt = ':8901/api/v0/register';
	this.dedicatedCloudResource = "";
	
	return this;
}

PortolSSEUtils.prototype.initialize = function(attrs){
	this.dedicatedCloudHost = attrs.dedicatedCloudHost;
	this.portolData = {playerId: attrs.playerId, btcAddr: attrs.btcPaymentAddr};	
	
	var playerid = this.portolData.playerId;
	var addr = this.portolData.btcAddr;
		
	var uri = "http://" + this.dedicatedCloudHost + this.apiUriExt + "?playerid=" + playerid + "&addr=" + addr;
	console.log(uri);
	
	this.establishConnection(uri);
	
	return this;
};

PortolSSEUtils.prototype.establishConnection = function(uri){
	var self = this;
	this.eventSource = new EventSource(uri);
	
	this.eventSource.addEventListener('message', function(event) {
			console.log("Server sent event: ", event);
			self.processSSEMessage(event.data);
		}, false);

	this.eventSource.addEventListener('open', function(open) {
		  // Connection was opened.
			console.log("SSE Open: ", open);
		}, false);

	this.eventSource.addEventListener('error', function(error) {
			console.log("Server sent error: ", error);
			self.processSSEError(event);
		}, false);
	
	return this;
};

PortolSSEUtils.prototype.processSSEMessage = function(message){
	console.log("SSE Message! " + message);
	return this;
};

PortolSSEUtils.prototype.processSSEError = function(error){
	  if (error.readyState == EventSource.CLOSED) {
		    console.log("Connection was closed.");
		  }
	return this;
};
