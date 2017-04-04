/*Too high level to follow .setup approach*/

function EndpointService(version, attrs, synchronizedStates){
	var self = this;
	this.utils = new EndpointUtils(version);
	
	this.playerState = synchronizedStates.playerState;
	
	this.pairingState = synchronizedStates.pairingState;
	
	this.accountState = synchronizedStates.accountState;
	
	var allStates = {"playerState": this.playerState,
			"pairingState": this.pairingState,
			"accountState": this.accountState};
	
	this.loadBalService = new LoadBalService(allStates);
	this.playerBuyService = new PlayerBuyService(allStates);
	this.favoriteService = new FavoriteService(allStates);
	this.wsUtils = new PortolWSUtils(allStates);
	
	this.god = {};
	
	this.eventBus = new EventBus(['initRequest', 
	                              'previewRequest', 
	                              'websocketPlay', 
	                              'websocketPause',
	                              'websocketStopped',
	                              'websocketOpen',
	                              'startPolling',
	                              'initReady',
	                              'previewReady',
	                              'videoReady',
	                              'paymentChange',
	                              'playerBuyRequest',
	                              'favoriteRequest',
	                              'bookmarkDeleteRequest',
	                              'freeviewTimeout',
	                              'loginAlert']);
	
	this.mapEvents().grantRights();
	
	this.eventBus.subscribe(this, ['initReady',
	                               'websocketPlay',
	                               'websocketPause',
	                               'videoReady',
	                               'freeviewTimeout',
	                               'loginAlert']);

	return this;
}

EndpointService.prototype.mapEvents = function(){
	/*
	 * endpointPlay - when the clickr presses play
	 * endpointPause - when the clickr presses pause
	 */
	//TODO: MUST FIX. It is possible that somebody listening to an event before playerState hears it,
	//			will onEvent it with stale playerState data.
	this.eventBus.subscribe(this.loadBalService, ['initRequest', 'previewRequest', 'startPolling', 'loginAlert']);
	this.eventBus.subscribe(this.playerBuyService, ['playerBuyRequest']);
	this.eventBus.subscribe(this.favoriteService, ['favoriteRequest', 'bookmarkDeleteRequest']);
	this.eventBus.subscribe(this.wsUtils, ['videoReady']);
	
	return this;
};

EndpointService.prototype.oninitReady = function(response){
	this.triggerInitReady(response);
	
	var state = this.playerState.getState();
	this.god.triggerStartPolling(state);
	
	return this;
};

EndpointService.prototype.onfreeviewTimeout = function(){
	var state = this.playerState.getState();
	this.god.triggerStartPolling(state);
	return this;
};

EndpointService.prototype.grantRights = function(){
	var self = this;

	this.wsUtils.triggerWebsocketOpen = function(socket){self.triggerEdgeWebsocketOpen(socket);};
	this.wsUtils.triggerWebsocketPlay = this.eventBus.trigger.bind(this.eventBus, 'websocketPlay');
	this.wsUtils.triggerWebsocketPause = this.eventBus.trigger.bind(this.eventBus, 'websocketPause');
	this.wsUtils.triggerFreeviewTimeout = this.eventBus.trigger.bind(this.eventBus, 'freeviewTimeout');
	this.wsUtils.triggerLoginAlert = this.eventBus.trigger.bind(this.eventBus, 'loginAlert');
	
	this.loadBalService.triggerInitReady = this.eventBus.trigger.bind(this.eventBus, 'initReady');
	this.loadBalService.triggerPreviewReady = this.eventBus.trigger.bind(this.eventBus, 'previewReady');
	this.loadBalService.triggerVideoReady = this.eventBus.trigger.bind(this.eventBus, 'videoReady');
	//this.loadBalService.triggerVideoReady = function(){self.triggerVideoReady();};
	
	this.playerState.triggerPaymentChange = this.eventBus.trigger.bind(this.eventBus, 'paymentChange');
	
	this.god.triggerStartPolling = this.eventBus.trigger.bind(this.eventBus, 'startPolling');
	this.god.triggerInitRequest = this.eventBus.trigger.bind(this.eventBus, 'initRequest');
	this.god.triggerPreviewRequest = this.eventBus.trigger.bind(this.eventBus, 'previewRequest');
	this.god.triggerPlayerBuyRequest = this.eventBus.trigger.bind(this.eventBus, 'playerBuyRequest');
	this.god.triggerFavoriteRequest = this.eventBus.trigger.bind(this.eventBus, 'favoriteRequest');
	this.god.triggerBookmarkDeleteRequest = this.eventBus.trigger.bind(this.eventBus, 'bookmarkDeleteRequest');
	
	return this;
};

EndpointService.prototype.doInit = function(){
	var state = this.playerState.getState();
	this.god.triggerInitRequest(state);
	
	return this;
};

EndpointService.prototype.onplayerBuyRequest = function(request){
	this.god.triggerPlayerBuyRequest(request);
	
	return this;
};

EndpointService.prototype.onfavoriteRequest = function(request){
	this.god.triggerFavoriteRequest(request);
	
	return this;
};

EndpointService.prototype.onbookmarkDeleteRequest = function(request){
	this.god.triggerBookmarkDeleteRequest(request);
	
	return this;
};

EndpointService.prototype.onvideoReady = function(response){
	this.triggerVideoReady(response);
	
	return this;
};

EndpointService.prototype.onwebsocketPlay = function(){
	this.triggerEndpointPlay();
	return this;
};

EndpointService.prototype.onwebsocketPause = function(){
	this.triggerEndpointPause();
	return this;
};

EndpointService.prototype.onpreviewRequest = function(){
	this.god.triggerPreviewRequest();
	return this;
};