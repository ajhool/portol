/**
 * Portol javascript library.
 */

/*
 * Suite:
 *  frameContainer -> contains the view modules
 *  endpointService -> contains the api modules
 *  god -> give event calls triggered "magically" a place to live
 *  Manager (Authenticated API key management for creating multiple players more quickly)
 *  	- v2.0 enhancement
 */

function Portol(attrs) {	
	this.version = "v0";
	this.modules = {};
	
	//this.vs_authInterface = new VS_AuthInterface();
	//this.vs_authInterface.start();
	
	this.playerState = new PlayerState();
	this.playerState.initialize();
	this.playerState.updateState({
		"apiKey": attrs.apiKey,
		"videoKey": attrs.videoKey,
		"referrerId": attrs.referrerId,
	});
	this.accountState = new AccountState();
	this.accountState.initialize();
	
	this.pairingState = new PairingState();
	
	//Define objects:
	this.frameContainer = new FrameContainer({
		"playerState": this.playerState,
		"accountState": this.accountState,
		"pairingState": this.pairingState
	});
	
	this.endpointService = new EndpointService(this.version, {
			"apiKey": attrs.apiKey,
			"videoKey": attrs.videoKey,
			"referrerId": attrs.referrerId
			},
			{
				"playerState": this.playerState,
				"accountState": this.accountState,
				"pairingState": this.pairingState,
			});
	
	this.god = {};
	
	//Map events:
	this.eventBus = new EventBus(['initRequest',
	                              'initReady',
	                              'previewRequest',
	                              'previewReady',
	                              'startPolling',
	                              'videoReady',
	                              'paymentChange',
	                              'login',
	                              'endpointPlay',
	                              'endpointPause',
	                              'playerBuyRequest',
	                              'favoriteRequest',
	                              'bookmarkDeleteRequest',
	                              'edgeWebsocketOpen']);
	
	this.mapEvents().grantRights();	
	
	//AJH DEBUG CAP
	this.endpointService.doInit();
	
	return this;
}

Portol.prototype.mapEvents = function(){
	/*
	 * initReady when api call to init responds with video information
	 * previewReady when api call to preview returns with an mpd
	 * videoReady when api call to start returns with an mpd
	 * paymentChange when api call to st
	 */
	this.eventBus.subscribe(this.frameContainer, ['initReady',
	                                              'previewReady',
	                                              'videoReady',
	                                              'paymentChange',
	                                              'endpointPlay',
	                                              'endpointPause',
	                                              'edgeWebsocketOpen']);
	
	this.eventBus.subscribe(this.endpointService, ['previewRequest',
	                                               'playerBuyRequest',
	                                               'favoriteRequest',
	                                               'bookmarkDeleteRequest']);
	
	return this;
};

Portol.prototype.grantRights = function(){
	this.frameContainer.triggerPreviewRequest = this.eventBus.trigger.bind(this.eventBus, 'previewRequest');
	this.frameContainer.triggerPlayerBuyRequest = this.eventBus.trigger.bind(this.eventBus, 'playerBuyRequest');
	this.frameContainer.triggerFavoriteRequest = this.eventBus.trigger.bind(this.eventBus, 'favoriteRequest');
	this.frameContainer.triggerLogin = this.eventBus.trigger.bind(this.eventBus, 'login');
	this.frameContainer.triggerBookmarkDeleteRequest = this.eventBus.trigger.bind(this.eventBus, 'bookmarkDeleteRequest');
	
	this.endpointService.triggerInitReady = this.eventBus.trigger.bind(this.eventBus, 'initReady');
	this.endpointService.triggerPreviewReady = this.eventBus.trigger.bind(this.eventBus, 'previewReady');
	this.endpointService.triggerVideoReady = this.eventBus.trigger.bind(this.eventBus, 'videoReady');
	this.endpointService.triggerPaymentChange = this.eventBus.trigger.bind(this.eventBus, 'paymentChange');
	this.endpointService.triggerEndpointPlay = this.eventBus.trigger.bind(this.eventBus, 'endpointPlay');
	this.endpointService.triggerEndpointPause = this.eventBus.trigger.bind(this.eventBus, 'endpointPause');
	this.endpointService.triggerEdgeWebsocketOpen = this.eventBus.trigger.bind(this.eventBus, 'edgeWebsocketOpen');
	
	return this;
};
	var api = {
	   registerPlayer: function (attrs) {
		   return new Portol(attrs);
	   },
	   
	   logDocumentation: function(){
		   console.log("documentation");
	   },       
	};
