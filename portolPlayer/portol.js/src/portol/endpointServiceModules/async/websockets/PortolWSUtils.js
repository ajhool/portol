function PortolWSUtils(synchronizedStates){
	this.apiUriExt = ':8901/ws';
	this.dedicatedCloudHost = "";
	
	this.playerState = synchronizedStates.playerState;
	this.pairingState = synchronizedStates.pairingState;
	this.accountState = synchronizedStates.accountState;
	
	this.paid = false;
	this.heartbeat = null;
		
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

PortolWSUtils.prototype.startHeartbeat = function(){
	var self = this;
	var lbCallback = function(){
		self.pSocket.send({'VSnotify': 'PING'});
		return;
	};
	this.heartbeat = setInterval(lbCallback, 10000);
	return this;
};

PortolWSUtils.prototype.endHeartbeat = function(){
	var self = this;
	clearInterval(self.heartbeat);
	self.heartbeat = null;
	
	return this;
};

PortolWSUtils.prototype.sendPlayerUpdate = function(playerUpdate){
	this.pSocket.send(JSON.stringify(playerUpdate));
	
	return this;
};

PortolWSUtils.prototype.mOnOpen = function(){
	this.triggerWebsocketOpen(this);
	this.startHeartbeat();
	
	return this;
};

PortolWSUtils.prototype.mOnClose = function(){
	this.triggerFreeviewTimeout();
	this.endHeartbeat();
	
	return this;
};
//{playerId: "ecf9a7da-d5a3-4c47-ad80-330ce36cbc", event: "LOGIN_ALERT"}
PortolWSUtils.prototype.establishConnection = function(uri){
	var self = this;
	this.pSocket = new WebSocket(uri);
	
	//this.pSocket.onopen = this.startHeartbeat.bind(this);
	this.pSocket.onopen = this.mOnOpen.bind(this);
	
	this.pSocket.onmessage = function(event){
		self.processSocketIncMessage(event.data);
	};
	
	this.pSocket.onerror = function(event){
		self.processSocketError(event);
	};
	
	this.pSocket.onclose = this.mOnClose.bind(this);
	
	return this;
};

PortolWSUtils.prototype.processPreviewTimeout = function(){
	console.log('Expect websocket to close.');
	return this;
};

PortolWSUtils.prototype.processSocketIncMessage = function(message){
	try {
		var m = JSON.parse(message);
	
		if(m.VSnotify){
			switch(m.VSnotify){
			case 'PONG':
				console.log('pong');
				break;
			default:
				console.log('Unhandled notification from VSnotify.');
			}
		}
		
		//console.log("status:", m.status);
		if(m.status){
			this.playerState.updateState({
				status: m.status,
			});
			
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
				case 'PREVIEW_TIMEOUT':
					console.log('URGENT: PREVIEW TIMEOUT!');
					this.processPreviewTimeout();
					break;
				default:
					console.log('URGENT: No status update in websocket message!', m);
					break;
			}
		}
		
		if(m.playerPayment){
			switch(m.playerPayment.status){
				case 'COMPLETE':
					this.paid = true;
					break;
				case 'PARTIAL':
					break;
				default:
					console.log("URGENT: No payment update in websocket message!", m);
					break;
			}
		}
		
		if("LOGIN_ALERT" === m.event){
			this.triggerLoginAlert();
		}
	} catch(e) {
		if('pong' === message){
			//do nothing
		}
	}
	
	return this;
};

PortolWSUtils.prototype.processPreviewTimeout = function(){
	
};

PortolWSUtils.prototype.processSocketError = function(error){
	  if (error.readyState == EventSource.CLOSED) {
		    console.log("Connection was closed.");
		  }
	return this;
};

PortolWSUtils.prototype.isPaid = function(){
	return this.paid;
};