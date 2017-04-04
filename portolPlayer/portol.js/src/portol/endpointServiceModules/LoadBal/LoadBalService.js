function LoadBalService(allStates, params){
	this.initResponse = null;
	this.previewResponse = null;
	this.startResponse = null;
		
	this.mapEvents().grantRights();
	
	this.playerState = allStates.playerState;
	this.pairingState = allStates.pairingState;
	this.accountState = allStates.accountState;
	
	this.loadBalWS = new LoadBalWebsocket(allStates);
	this.loadBalRequest = new LoadBalRequest(this.playerState);
	
	this.isPlatformOwned = false;
	this.isPlatformPaired = false;
	
	this.isPolling = false;
	
	this.isLoggedIn = false;
	this.noCurrentPlatform = true;
	
	return this;
}

LoadBalService.prototype.mapEvents = function(){
	//no downstream events
	return this;
};

LoadBalService.prototype.grantRights = function(){
	//no rights
	return this;
};

LoadBalService.prototype.queryStart = function(){
	var timeToNextQuery = 3000;
	
	var dest = "start";
	
	var onsuccess = this.processStartResponse.bind(this, timeToNextQuery);
	var onerror = function(error){console.log("Start Error: ", error);};
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.loadBalRequest.makeRequest(dest, callbacks);
	
	return this;
};

LoadBalService.prototype.queryUser = function(){
	var timeToNextQuery = 3000;
	
	var dest = "user";
	
	var onsuccess = this.processUserResponse.bind(this, timeToNextQuery);
	var onerror = function(error){console.log("User Error: ", error);};
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.loadBalRequest.makeRequest(dest, callbacks);
	
	return this;
};

LoadBalService.prototype.processUserResponse = function(timeToNextQuery, response){
	var self = this;
	
	var r = response || {};
	console.log('user query', r);
	
	if(this.checkLoggedIn(r)) {
		console.log("User cleared");
		clearTimeout(self.userPoll);
		//TODO: If user exists, we should open up the websocket.
		//this.portolWS.initialize(response);
		
		//WARNING! THIS IS ONLY TEMPORARY. PLEASE UNCOMMENT!
		this.accountState.updateState(r);
		/*
		this.accountState.eventBus.trigger('newUser');
		*/
	} else {
		console.log("Platform not owned.");
		self.userPoll = setTimeout(function(){self.queryUser();}, timeToNextQuery);
	}
};

LoadBalService.prototype.onstartPolling = function(){
	this.isPolling = true;
	this.queryStart();
	
	return this;
};

LoadBalService.prototype.onpreviewRequest = function(){
	var dest = "preview";
	
	var onsuccess = this.processPreviewResponse.bind(this);
	var onerror = function(error){console.log("Preview Error: ", error);};
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.loadBalRequest.makeRequest(dest, callbacks);
	
	return this;
};

LoadBalService.prototype.oninitRequest = function(){
	var self = this;
	var dest = "init";
	
	//var onsuccess = self.triggerInitReady;
	//var onsuccess = function(response){self.triggerInitReady(response)};
	var onsuccess = this.processInitResponse.bind(this);
	var onerror = this.processInitError.bind(this);
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.loadBalRequest.makeRequest(dest, callbacks);
	
	return this;
};

LoadBalService.prototype.onloginAlert = function(){
	this.queryStart();
	
	return this;
};

LoadBalService.prototype.processStartResponse = function(timeToNextQuery, response){
	var self = this;
	
	var r = response;
	if(this.checkPayment(response)) {
		clearTimeout(self.startPoll);
		this.isPolling = false;
		
		this.playerState.updateState({
			'dedicatedCloudHost': r.dedicatedCloudHost,
			'status': r.newStatus,
			'mpdAuthorized': r.mpdAuthorized,
			'hostPlatform': r.hostPlatform,
		});
		
		//this.playerState.metaData = response.metaData;
		
		
		this.pairingState.updateState({
			castColor: r.hostPlatform.platformColor,
			qrcodeURL: r.qrURL
		});
		
		/*
		if(r.hostPlatform.platformColor && r.hostPlatform.platformColor !== 'orphaned') {
			document.getElementById('pairing-button-icon').style.background = "#" + r.hostPlatform.platformColor;
		}
		*/
		this.accountState.updateState(r.loggedIn);
		
		this.triggerVideoReady(response);
	} else if(!this.isPolling){ //previewMode
		clearTimeout(self.startPoll);	
	} else {
		self.startPoll = setTimeout(function(){self.queryStart();}, timeToNextQuery);
	}
	
	if(this.noCurrentPlatform && this.checkPlatformOwned(response)){
		this.playerState.stateEventBus.trigger('platformClaimed');
		this.queryUser();
		
		this.noCurrentPlatform = false;
	}
	
	if(!this.noCurrentPlatform && !this.isPlatformPaired && this.checkPlatformPaired(response)) {
		this.loadBalWS.initialize();
		this.isPlatformPaired = true;
		console.log('Opening loadbal webscoket.');
	}
	
	return this;
};

LoadBalService.prototype.processPreviewResponse = function(response){
	//this.triggerPreviewReady(response);
	var self = this;
	
	var r = response;
	
	this.playerState.updateState({
		'dedicatedCloudHost': r.dedicatedCloudHost,
		'status': r.newStatus,
		'mpdAuthorized': r.mpdAuthorized,
		'hostPlatform': r.hostPlatform,
	});
		
	//this.playerState.metaData = response.metaData;
		
	this.pairingState.updateState({
		castColor: r.hostPlatform.platformColor,
		qrcodeURL: r.qrURL
	});
		
	this.accountState.updateState(r.loggedIn);
		
	this.triggerVideoReady(response);
	
	clearTimeout(self.startPoll);
	this.isPolling = false;
	
	if(this.noCurrentPlatform && this.checkPlatformOwned(response)){
		this.playerState.stateEventBus.trigger('platformClaimed');
		this.queryUser();
		
		this.noCurrentPlatform = false;
	}
	
	if(!this.noCurrentPlatform && !this.isPlatformPaired && this.checkPlatformPaired(response)) {
		this.loadBalWS.initialize();
		this.isPlatformPaired = true;
		console.log('Opening loadbal websocket.');
	}
	
	return this;
};

LoadBalService.prototype.processInitResponse = function(response){
	console.log("Init response: ", response);
	
	var r = response;
	this.playerState.updateState({
		'apiKey': r.apiKey,
		'btcAddress': r.btcPaymentAddr,
		'status': r.newStatus,
		'playerId': r.playerId,
		'videoKey': r.videoKey,
		'id': r.playerId,
		'hostPlatform': r.hostPlatform,
		//'loggedIn': r.loggedIn,
	});	
	
	console.log('state after init', this.playerState);
	
	this.playerState.metaData = response.metaData;
	
	var mTextPair = (typeof r.playerId === 'string') ? r.playerId.substring(0,5) : null;
	
	this.pairingState.updateState({
		castColor: r.color,
		qrcodeURL: r.qrURL,
		textPairCode: mTextPair,
	});
		
	if(r.hostPlatform.platformColor && r.hostPlatform.platformColor !== 'orphaned') {
		document.getElementById('pairing-button-icon').style.background = "#" + r.hostPlatform.platformColor;
	}
	
	this.accountState.updateState(r.loggedIn);
	
	this.triggerInitReady(response);
	
	return this;
};

LoadBalService.prototype.processInitError = function(error){
	//TODO: Try 3 times before failing.
	
	var e = error;
	switch(e.status){
		case 303:
			window.setTimeout(this.oninitRequest.bind(this), 200);
			break;
		default:
			console.log("Init error: ", error);
	}
	
	return this;
};

LoadBalService.prototype.checkPayment = function(response){
	var isPaid = false;
	
	if(response.mpdAuthorized) {
		isPaid = true;
	}
	
	return isPaid;
};


LoadBalService.prototype.checkLoggedIn = function(response){
	var r = response || {};
	
	if(r.userName){
		 this.isLoggedIn = true;
	}
	
	return this.isLoggedIn;
};

LoadBalService.prototype.checkPlatformOwned = function(response){	
	if(!this.isPlatformOwned) {
		var r = response || {};
		if(r.hostPlatform.platformColor !== 'orphaned'){
			 this.isPlatformOwned = true;
		}
	}
	
	return this.isPlatformOwned;
};

LoadBalService.prototype.checkPlatformPaired = function(response) {
	var r = response || {};
	if(r.hostPlatform.paired){
		 this.isPlatformPaired = true;
	}
	
	return this.isPlatformPaired;
};
