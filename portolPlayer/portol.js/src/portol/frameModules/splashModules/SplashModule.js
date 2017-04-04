function SplashModule(context) {
	//this.screenElement = document.getElementById('screen-container');
	this.screenElement = document.body;
	
	this.god = {};
	
	this.initialize(context);
	
	this.eventBus.subscribe(this, ['playerBuyRequest',
	                               'previewRequest',
	                               'favoriteRequest',
	                               'bookmarkDeleteRequest']);
	
	return this;
}

SplashModule.prototype.initialize = function(synchronizedStates){

	this.svgs = new Portol_SVGSymbolContainer();
	
	this.accountState = synchronizedStates.accountState;
	this.playerState = synchronizedStates.playerState;
	this.pairingState = synchronizedStates.pairingState;
		
	this.userModule = new UserInfoContainer(synchronizedStates.accountState);
	this.userModule.initialize();
	this.videoModule = new VideoInfoContainer(synchronizedStates.playerState);
	this.videoModule.initialize();
	this.pairingModule = new PairingContainer(synchronizedStates.pairingState);
	this.pairingModule.initialize();
	this.portolModule = new PortolContainer();
	this.portolModule.initialize();
	
	this.buildElement();
	/*
	 * DOM holders:
	 * splashPanel -- holds module info panels
	 */
	
	this.eventBus = new EventBus(['clickrAdded', 
	                              'videoChange', 
	                              'previewRequest',
	                              'favoriteRequest',
	                              'bookmarkDeleteRequest',
	                              'rotateCarousel',
	                              'observableScroll']);
	
	this.mapEvents().grantRights();
	
	this.eventBus.subscribe(this, ['observableScroll']);
	
	return this;
};

SplashModule.prototype.grantRights = function(){
	var self = this;
	
	this.videoModule.triggerPreviewRequest = this.eventBus.trigger.bind(this.eventBus, 'previewRequest');
	this.videoModule.triggerFavoriteRequest = this.eventBus.trigger.bind(this.eventBus, 'favoriteRequest');
	this.videoModule.triggerBookmarkDeleteRequest = this.eventBus.trigger.bind(this.eventBus, 'bookmarkDeleteRequest');
	
	this.god.triggerVanish = this.eventBus.trigger.bind(this.eventBus, 'vanish');
	
	return this;
};

SplashModule.prototype.mapEvents = function(){
	/*
	 * onstaticRender is triggered when the video is first displayed and after a video ends.
	 */
	
	this.eventBus.subscribe(this.pairingModule, ['clickrAdded']);
	this.eventBus.subscribe(this.videoModule, ['videoChange']);
	this.eventBus.subscribe(this.shimGradient, ['observableScroll']);
	
	return this;
};

SplashModule.prototype.oninitSplash = function(splashParams){
	this.initialize(splashParams).buildElement();
	
	return this;
};

SplashModule.prototype.buildElement = function(){
	this.buildScreenElement();
	return this;
};

SplashModule.prototype.buildScreenElement = function(){
	//TODO: Make the panel element responsible for this DOM nonsense.
	var defaultImage = 'img/theater.jpg';
	
	var bg = this.playerState.metaData.splashURL || defaultImage;
	console.log('background image,', bg);
	//this.screenElement.style.backgroundImage = 'url(' + bg + ')';
	this.backdropElement = document.createElement('img');
	this.backdropElement.id = 'theme-backdrop';
	this.backdropElement.classList.add('fixed-backdrop');
	this.backdropElement.src = bg;
	
	//var target = this.screenElement;
	//var target = document.getElementById('panels-container');
	var target = document.getElementById('video-info-panel');
	target.insertBefore(this.backdropElement, target.firstChild);
	
	return this;
};

SplashModule.prototype.onstartVideo = function(){
	//this.screenElement.style.display = 'none';
	//this.screenElement.style.display='';
	this.backdropElement.classList.add('dimming-gradient');
	document.getElementById('shim-gradient').classList.add('dimmed');
	/*
	var darkBack = document.getElementById('dark-backdrop');
	darkBack.classList.remove('no-filter');
	darkBack.classList.add('dimming-filter');
	*/
	var toHide = document.getElementsByClassName('remove-for-play');
	
	if(toHide){
		for(var ctr = 0; ctr < toHide.length; ctr++){
			toHide[ctr].classList.add('is-removed');
		}
	}
	
	return this;
};

SplashModule.prototype.onportolSSE = function(data){
	console.log("Splash module saw portolSSE");
	return this;
};

SplashModule.prototype.onplayerBuyRequest = function(){
	this.triggerPlayerBuyRequest();
	return this;
};

SplashModule.prototype.onpreviewRequest = function(){
	this.triggerPreviewRequest();
	return this;
};

SplashModule.prototype.onfavoriteRequest = function(){
	this.triggerFavoriteRequest();
	return this;
};

SplashModule.prototype.onbookmarkDeleteRequest = function(){
	this.triggerBookmarkDeleteRequest();
	return this;
};

SplashModule.prototype.getPanels = function(){
	return {
		"videoInfo": this.videoModule,
		"account": this.userModule,
		"pairing": this.pairingModule,
		"portolInfo": this.portolModule
	};
};