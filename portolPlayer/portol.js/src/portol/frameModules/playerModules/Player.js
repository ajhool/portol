function Player(playerState) {
	var context = new Dash.di.DashContext();
	this.dash = new MediaPlayer(context);
	
	this.dedicatedCloudHost = null;
	this.portolData = null;
	//this.dash.attachPortolData({playerId: playerId, btcAddr: btcAddr});
	//TODO: This had been player.injectIdentification
	//var id = {playerId: attrs.response.playerId, btcAddr: attrs.response.btcPaymentAddr};
	//this.dash.attachPortolData(id);
	this.element = document.getElementById('pdash-player');
	
	//throttle is used to send time updates to edge no quicker than once per THROTTLE_TIME.
	this.throttle = false;
	this.THROTTLE_TIME = 1000; //1 second
	
	this.paid = false;
	this.blockingForPayment = false;

	this.playerState = playerState;
	console.log('playerState');
	
	return this;
}

Player.prototype.createPaymentListener = function(){
	var self = this;
	
	this.edgeWebsocket.watch('paid', function(field, oldVal, newVal){
		self.setIsPaid(newVal);
	});
};

Player.prototype.setIsPaid = function(paid){
	this.paid = paid;
	
	if(paid){
		this.triggerPaymentSeen();
		if(this.blockingForPayment){
			this.unblockPayments();
		}
	}
	
	return this;
};

Player.prototype.unblockPayments = function(){
	
	this.doPlay();
	
	return this;
};

Player.prototype.setup = function(attrs) {
	//TODO: Shouldn't this be dedicatedCloudHost?
	this.dedicatedCloudHost = attrs.dedicatedCloudHost;
	this.portolData = attrs.portolData;
	
	return this;
};

Player.prototype.hasRequiredParams = function(attrs) {
	attrs = attrs || {};
	var validity = ((typeof attrs.apiKey !== 'undefined') &&
				  (null !== attrs.apiKey));
	
	return validity;
};

Player.prototype.loadManifest = function(){
		
	var mpdLocation = this.dedicatedCloudHost;
	this.playerId = this.portolData.playerId;
	var url = "http://" + mpdLocation + ":8901/api/v0/mpd/vod/" + this.playerId + "/";
	
	this.dash.startup();
	
	this.dash.attachView(this.element);
	this.dash.attachSource(url, null, null, this.portolData);
	
	var self = this;
	
	this.element.addEventListener("timeupdate", function(timeupdate){
		self.processTimeUpdate(this.currentTime, this.duration);
	});
	
	var state = this.playerState.metaData;
	this.hardStopTime = state.secondsFree;
	
	this.element.addEventListener('click', this.doPlayPauseToggle.bind(this));
	
	return this;
};

Player.prototype.processHardStop = function(currentTime){
	if((currentTime >= this.hardStopTime) & !this.edgeWebsocket.isPaid()) {
		this.blockingForPayment = true;
		this.doPause();
		this.triggerFreeviewEnd();
	}
	
	return this;
};

Player.prototype.processTimeUpdate = function(currentTime, totalTime){
	
	this.processHardStop(currentTime);
		
	var sStatus = {
		'progress': currentTime,
		'streamDuration': totalTime,
		'remaining': totalTime-currentTime,
	};
	
	var mPlayerId = this.playerId;
	var playerInformation = {
			'playerId': mPlayerId,
			'event': 'SEEK_UPDATE',
			'sStatus': sStatus,
	};
	
	//console.log('time Information', timeInformation);
	if(!this.throttle){
		this.edgeWebsocket.sendPlayerUpdate(playerInformation);
		this.throttle = true;
		var self = this;
		this.unthrottle = setTimeout(function(){self.throttle = false;}, self.THROTTLE_TIME);
	}
	
	return this;
};


Player.prototype.updateManifest = function(manifestXML) {
	var srcObject = {mpdfile: manifestXML, isXML: true};
	this.dash.attachSource(srcObject, null, null, this.portolData);
	
	return this;
};

Player.prototype.startPlay = function() {
	this.dash.play();
	return this;
};

Player.prototype.doPlay = function(){
	var video = this.dash.getVideoModel();
	
	if(video) {
		video.play();
	} else {
		console.log("No video detected.");
	}
	
	return this;
};

Player.prototype.doPause = function(){
	var video = this.dash.getVideoModel();
	
	if(video) {
		video.pause();
	} else {
		console.log("No video detected.");
	}
	
	return this;
};

Player.prototype.doPlayPauseToggle = function(){
	var video = this.dash.getVideoModel();
	
	if(video) {
		if(video.isPaused()){
			video.play();
		} else {
			video.pause();
		}
	}
	
	return this;
};