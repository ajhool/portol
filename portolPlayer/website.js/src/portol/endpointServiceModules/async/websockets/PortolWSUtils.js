function PortolWSUtils(synchronizedStates){
	this.apiUriExt = ':8901/ws';
	this.dedicatedCloudHost = "";
	
	this.playerState = synchronizedStates.playerState;
	this.pairingState = synchronizedStates.pairingState;
	this.accountState = synchronizedStates.accountState;
	
	return this;
}

PortolWSUtils.prototype.onvideoReady = function(){
	this.initialize();
	
	return this;
};

PortolWSUtils.prototype.initialize = function(){
	
	var current = this.playerState.getStateObject();
	
	this.dedicatedCloudHost = current.dedicatedCloudHost;
	this.portolData = {playerId: current.playerId, btcAddr: current.btcAddress};	
	
	var playerid = this.portolData.playerId;
	var addr = this.portolData.btcAddr;
		
	var uri = "ws://" + this.dedicatedCloudHost + this.apiUriExt + "?playerid=" + playerid + "&addr=" + addr;
	console.log(uri);
	
	this.establishConnection(uri);
	
	return this;
};

PortolWSUtils.prototype.startHeartbeat = function(event){
	var self = this;
	var lbCallback = function(){
		self.pSocket.send('--heartbeat--');
		return;
	};
	this.heartbeat = setInterval(lbCallback, 10000);
	return this;
};

PortolWSUtils.prototype.establishConnection = function(uri){
	var self = this;
	this.pSocket = new WebSocket(uri);
	
	this.pSocket.onopen = this.startHeartbeat.bind(this);
	
	this.pSocket.onmessage = function(event){
		self.processSocketIncMessage(event.data);
	};
	
	this.pSocket.onmessage = function(event){
		self.processSocketIncMessage(event.data);
	};
	
	this.pSocket.onerror = function(event){
		self.processSocketError(event);
	};
	
	return this;
};

PortolWSUtils.prototype.processSocketIncMessage = function(message){
	//console.log("Web socket message Message! ", message);
	
	var m = JSON.parse(message);
	
	this.playerState.updateState({
		status: m.status,
	});
	
	//console.log("status:", m.status);
	
	switch(m.status){
		case 'STREAMING':
			this.triggerWebsocketPlay();
			break;
		case 'PAUSED':
			this.triggerWebsocketPause();
			break;
		case 'STOPPED':
			this.triggerWebsocketStopped();
			break;
		default:
			console.log('URGENT: No status update in websocket message!', m);
			break;
	}
	
	return this;
};

PortolWSUtils.prototype.processSocketError = function(error){
	  if (error.readyState == EventSource.CLOSED) {
		    console.log("Connection was closed.");
		  }
	return this;
};
/*
PortolWSUtils.prototype.onwebsocketPlay = function(){
	this.triggerEndpointPlay();
	
	return this;
};

PortolWSUtils.prototype.onwebsocketPause = function(){
	this.triggerEndpointPause();
	
	return this;
};
*/