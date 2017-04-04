/*
 * This is just a DOM element wrapper to hold buttons and control their display.
 */

function ControlBar(attrs) {
	var a = attrs || {};
	this.element = document.getElementById('control-bar');
	
	this.drawerButton = document.getElementById('drawer-controller');
	this.watchButton = new WatchButton(attrs);
	this.videoInfoButton = new VideoInfoButton(attrs);
	this.pairingButton = new PairingButton(attrs);
	this.accountButton = new AccountButton(attrs);
	//this.portolButton = new PortolButton(attrs);
	
	this.buttons = [this.watchButton,
	                this.videoInfoButton,
	                this.pairingButton,
	                this.accountButton];
	                //this.portolButton];
	
	this.isOpen = a.open || true;
	
	this.eventBus = new EventBus(['playerBuyRequest',
	                              'favoriteRequest',
	                              'bookmarkDeleteRequest',
	                              'controlClick']);
	
	this.grantRights()
		.mapEvents();
	
	this.eventBus.subscribe(this, ['controlClick', 'playerBuyRequest']);
	
	var self = this;
	
	return this;
}

ControlBar.prototype.initialize = function(){
	this.stateHandlerSetup();
	return this;
};

ControlBar.prototype.toggleMiniButtons = function(){
	var ctr = 0;
	for (ctr = 0; ctr < this.buttons.length; ctr++) {
		var target = this.buttons[ctr];
		target.toggleMini();
	}
	
	return this;
};

ControlBar.prototype.closeDrawer = function(){
	//TODO: Check if openDrawer already there.
	//TODO: Animate
	if(this.isOpen) {
		//this.element.className = this.element.className + 'closed-drawer';
		this.element.classList.toggle('closed-drawer');
		this.element.classList.toggle('opened-drawer');
		
		this.toggleMiniButtons();
	}
	this.isOpen = false;
	return this;
};

ControlBar.prototype.openDrawer = function(){
	//TODO: Check if openDrawer already there.
	//TODO: Animate
	
	if(!this.isOpen) {
		this.element.classList.toggle('closed-drawer');
		this.element.classList.toggle('opened-drawer');
		
		this.toggleMiniButtons();
	}
	this.isOpen = true;
	return this;
};

ControlBar.prototype.hideDrawer = function(){
	this.element.classList.remove('closed-drawer');
	this.element.classList.remove('opened-drawer');
	this.element.classList.add('hidden-drawer');
	
	return this;
};

ControlBar.prototype.toggleDrawer = function(){
	if(this.isOpen) {
		this.closeDrawer();
	} else {
		this.openDrawer();
	}
	
	return this;
};

ControlBar.prototype.grantRights = function(){
	this.watchButton.triggerControlClick = this.eventBus.trigger.bind(this.eventBus, 'controlClick');
	this.videoInfoButton.triggerControlClick = this.eventBus.trigger.bind(this.eventBus,'controlClick');
	this.pairingButton.triggerControlClick = this.eventBus.trigger.bind(this.eventBus, 'controlClick');
	this.accountButton.triggerControlClick = this.eventBus.trigger.bind(this.eventBus, 'controlClick');
	//this.portolButton.triggerControlClick = this.eventBus.trigger.bind(this.eventBus, 'controlClick');
	
	//this.watchButton.triggerPlayerBuyRequest = this.eventBus.trigger.bind(this.eventBus, 'playerBuyRequest');
	//this.previewButton.triggerPreviewRequest = this.eventBus.trigger.bind(this.eventBus, 'previewRequest');
	//this.previewButton.triggerFavoriteRequest = this.eventBus.trigger.bind(this.eventBus, 'favoriteRequest');
	//this.portolButton.triggerBookmarkDeleteRequest = this.eventBus.trigger.bind(this.eventBus, 'bookmarkDeleteRequest');

	return this;
};

ControlBar.prototype.mapEvents = function(){	
	//No listeners here.
	
	return this;
};

ControlBar.prototype.oncontrolClick = function(attrs){
	this.triggerRotateCarousel(attrs.panel);
	return this;
};

ControlBar.prototype.onobservableScroll = function(observed){
	
	//console.log('control bar sees observed', observed);
	var panels = observed.panels;
	var carousel = observed.carousel;
	
	///****
	var modules = Object.keys(panels);
	var ctr = 0;
	
	for (ctr = 0; ctr < modules.length; ctr++) {
		var panelName = modules[ctr];
		var target = panels[panelName];
		target.name = panelName;
		
		var POI = target;
		var bCtr = 0;
		for(bCtr = 0; bCtr < this.buttons.length; bCtr++){
			var BOI = this.buttons[bCtr];
			//console.log(POI, BOI);
			if(POI.name == BOI.panel){
				if(POI.inFocus) {
					BOI.focus();
				} else {
					BOI.unfocus();
				}
			}
		}
	}
	
	return this;
};

ControlBar.prototype.onplayerBuyRequest = function(){
	this.triggerPlayerBuyRequest();
	return this;
};

ControlBar.prototype.stateHandlerSetup = function(){
	var self = this;
	//this.drawerButton.addEventListener('click', this.toggleDrawer.bind(this));
	this.element.addEventListener('click', this.toggleDrawer.bind(this));
	return this;
};

ControlBar.prototype.onshyify = function(){
	this.element.classList.add('hide-drawer');
	this.element.classList.remove('closed-drawer');
	this.element.classList.remove('opened-drawer');
	
	return this;
};

ControlBar.prototype.onstopShyify = function() {
	if(this.isOpen) {
		//this.element.className = this.element.className + 'closed-drawer';
		this.element.classList.toggle('closed-drawer');
		this.element.classList.toggle('opened-drawer');
		
		this.toggleMiniButtons();
	}
};

ControlBar.prototype.onplatformClaimed = function(){
	
};