function LoadBalWebsocket(synchronizedStates){
	this.baseUrl = 'wss://www.portol.me:8443';
	this.version = 'v0';
	
	this.currentPayment = 0;
	
	this.playerState = synchronizedStates.playerState;
	this.pairingState = synchronizedStates.pairingState;
	this.accountState = synchronizedStates.accountState;
	
	this.pSocket = null;
	
}

LoadBalWebsocket.prototype.buildUrl = function(extension) {
	var params = this.playerState.getStateObject();
	
	var mAddress = params.btcAddress;
	var mPlayerId = params.playerId;
	
	return this.baseUrl + "/" + extension + "?" + "addr=" + mAddress + "&playerid=" + mPlayerId;
};

LoadBalWebsocket.prototype.initialize = function(){
	this.establishConnection(this.buildUrl('ws'));
	
	return this;
};

LoadBalWebsocket.prototype.startHeartbeat = function(event){
	var self = this;
	var lbCallback = function(){
		self.pSocket.send('--heartbeat--');
		return;
	};
	this.heartbeat = setInterval(lbCallback, 10000);
	return this;
};

LoadBalWebsocket.prototype.establishConnection = function(uri){
	var self = this;
	this.pSocket = new WebSocket(uri);
	
	this.pSocket.onopen = this.startHeartbeat.bind(this);
	
	this.pSocket.onmessage = function(event){
		//console.log("Message from server: ", event);
		self.processSocketIncMessage(event.data);
	};
	
	this.pSocket.onerror = function(event){
		self.processSocketError(event);
	};
	
	return this;
};

LoadBalWebsocket.prototype.processSocketError = function(error){
	console.log("LoadBal error", error);
	return this;
};

LoadBalWebsocket.prototype.processSocketIncMessage = function(message){
	console.log('incoming message', message);
	return this;
};
