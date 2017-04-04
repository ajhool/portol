function VideoInfoContainer(playerState) {
	PanelContainer.call(this, {
		name: 'videoInfoPanel',
		panel: 'videoInfo',
		elementId: 'video-panel',
	});
	
	this.playerState = playerState;
	
	this.element = document.getElementById('video-info-panel');
}

VideoInfoContainer.prototype = Object.create(PanelContainer.prototype);
VideoInfoContainer.prototype.constructor = VideoInfoContainer;

VideoInfoContainer.prototype.initialize = function(){
	/*
	 * 
	 * TODO:
			numPlays: splashParams.numPlays,
			numLoads: splashParams.numLoads,
			currentViewers: splashParams.currentViewers,
			seriesInfo: splashParams.seriesInfo,
			epg: splashParams.epg
	*/
	//var videoParams = this.playerState.getStateObject();
	var attrs = this.playerState.metaData || {};
	
	console.log('metaData', attrs);
			
	var theTitle = attrs.currentTitle || attrs.channelOrVideoTitle;
	this.videoTitle = new VideoTitle(theTitle);
	this.videoDescription = new VideoDescription(attrs.info);
	
	this.videoRating = new VideoRating(attrs.rating);	
	this.videoCreator = new VideoCreator(attrs.creatorInfo);
	
	//TODO: EPG stuff
	//this.priceScroll = new PriceScroll(130*3, 80, attrs.prices);
	this.price = new Price(attrs.prices);
	
	//TODO: Port to new controlButton setup
	//this.controlButton = new ContentInfoSymbol();
	
	this.channelInfo = new ChannelInfo({
		name: attrs.channelName,
		description: attrs.channelDescription
	});

	this.eventBus = new EventBus();
	
	this.mapEvents().grantRights();
	
	return this;
};

VideoInfoContainer.prototype.grantRights = function(){

	return this;
};

VideoInfoContainer.prototype.mapEvents = function(){
	//this.eventBus.subscribe(this.controlButton, ['controlClick']);
	//	this.eventBus.subscribe(this.panel, ['controlClick']);

	this.eventBus.subscribe(this, ['previewRequest',
	                               'playerBuyRequest',
	                               'favoriteRequest',
	                               'bookmarkDeleteRequest',
	                               'changeFocus']);
	
	return this;
};

VideoInfoContainer.prototype.getButtonElement = function(){
	return this.controlButton.getElement();
};

VideoInfoContainer.prototype.getElement = function(){
	return this.element;
};

VideoInfoContainer.prototype.getChannelElement = function(){
	return this.channelInfo.getElement();
};

VideoInfoContainer.prototype.onplayerBuyRequest = function(){
	this.triggerPlayerBuyRequest();
	return this;
};

VideoInfoContainer.prototype.onfavoriteRequest = function(){
	this.triggerFavoriteRequest();
	return this;
};

VideoInfoContainer.prototype.onpreviewRequest = function(){	
	this.triggerPreviewRequest();
	return this;
};

VideoInfoContainer.prototype.onbookmarkDeleteRequest = function(){
	this.triggerBookmarkDeleteRequest();
	return this;
};

VideoInfoContainer.prototype.onrotateCarousel = function(focus){
	this.triggerChangeFocus(focus);
	return this;
};

