function PlayerContainer(synchronizedStates){
	PanelContainer.call(this, {
				name: "watchButton",
				panel: 'player',
				elementId: "player-container"
			});
	
	//this.triggerPreviewRequest = function(){console.log("Frame container original triggerPreviewRequest()")};
	this.playerState = synchronizedStates.playerState;
	this.pairingState = synchronizedStates.pairingState;
	this.accountState = synchronizedStates.accountState;
	
	this.player = new Player(synchronizedStates.playerState);
	this.on = false;
	
	this.playerOptions = new PlayerOptions();
	this.playerBuyButton = new PlayerBuyButton();
	this.playerPreviewButton = new PlayerPreviewButton();
	
	this.preMessage = new PreMessage();
	
	this.socketPipeline = null;
	
	return this;
}

PlayerContainer.prototype = Object.create(PanelContainer.prototype);
PlayerContainer.prototype.constructor = PlayerContainer;

PlayerContainer.prototype.initialize = function(){
	
	this.playerBuyButton.addHandlers();
	this.preMessage.addHandlers();
	this.playerPreviewButton.addHandlers();
	this.eventBus = new EventBus(['start', 'play', 'pause', 'stop', 'playerBuyRequest', 'previewRequest', 'freeviewEnd', 'paymentSeen', 'afterFreeviewPayment']);

	this.mapEvents().grantRights();
	
	return this;
};

PlayerContainer.prototype.grantRights = function(){
	//No children.
	this.playerBuyButton.triggerPlayerBuyRequest = this.eventBus.trigger.bind(this.eventBus, 'playerBuyRequest');
	this.playerBuyButton.triggerAfterFreeviewPayment = this.eventBus.trigger.bind(this.eventBus,'afterFreeviewPayment');
	
	this.playerPreviewButton.triggerPreviewRequest = this.eventBus.trigger.bind(this.eventBus, 'previewRequest');
	
	this.player.triggerFreeviewEnd = this.eventBus.trigger.bind(this.eventBus, 'freeviewEnd');
	this.player.triggerPaymentSeen = this.eventBus.trigger.bind(this.eventBus, 'paymentSeen');
	
	return this;
};

PlayerContainer.prototype.addEdgeWebsocket = function(socket){
	this.edgeWebsocket = socket;
	
	this.player.edgeWebsocket = socket;
	this.player.createPaymentListener();
	
	return this;
};

PlayerContainer.prototype.mapEvents = function(){
	/*
	 * onstaticRender is triggered when the video is first displayed and after a video ends.
	 */
	this.eventBus.subscribe(this.player, ['start', 'play', 'pause', 'stop']);
	this.eventBus.subscribe(this, ['playerBuyRequest', 'previewRequest', 'freeviewEnd', 'paymentSeen', 'afterFreeviewPayment']);
	
	return this;
};

//TODO: install manifest here too?
PlayerContainer.prototype.onstartVideo = function(){

	var attrs = this.playerState.getStateObject();
	
	if(!this.on) {
		this.player.setup({
			"dedicatedCloudHost": attrs.dedicatedCloudHost,
			"portolData": {
				"playerId": attrs.playerId,
				"btcAddr": attrs.btcAddress
			}
		});
		this.on = true;
	}

	this.playerOptions.hide();
	this.element.appendChild(this.player.element);
	
	//TODO: Build url for mpd
	//var url = response.dedicatedCloudHost + '/' + response.
	this.player.loadManifest();
	
	return this;
};

PlayerContainer.prototype.onpreviewReady = function(response){
	if(!this.on) {
		this.player.setup();
		this.on = true;
	}
	
	this.element.appendChild(this.video.element);
	
	return this;
};

PlayerContainer.prototype.onendpointPlay = function(){
	this.player.doPlay();
	
	return this;
};

PlayerContainer.prototype.onendpointPause = function(){
	this.player.doPause();
	
	return this;
};

PlayerContainer.prototype.onplayerBuyRequest = function(){
	this.triggerPlayerBuyRequest();
	/*
	var beforeFreeview = document.getElementsByClassName('show-before-freeview');
	
	for(var ctr = 0; ctr<beforeFreeview.length; ctr++){
		beforeFreeview[ctr].classList.remove('is-removed');
	}
	
	var toHide = document.getElementsByClassName('remove-for-play');
	for(var hideCtr = 0; hideCtr < toHide.length; hideCtr++){
		toHide[hideCtr].classList.add('is-removed');
	}
	
	var duringFreeview = document.getElementsByClassName('show-during-freeview');
	
	for(var duringCtr = 0; duringCtr<duringFreeview.length; duringCtr++){
		duringFreeview[duringCtr].classList.remove('is-removed');
	}
	*/
	
	return this;
};

PlayerContainer.prototype.onpreviewRequest = function(){
	
	var beforeFreeview = document.getElementsByClassName('show-before-freeview');
	
	for(var ctr = 0; ctr<beforeFreeview.length; ctr++){
		beforeFreeview[ctr].classList.remove('is-removed');
	}
	
	var toHide = document.getElementsByClassName('remove-for-play');
	for(var hideCtr = 0; hideCtr < toHide.length; hideCtr++){
		toHide[hideCtr].classList.add('is-removed');
	}
	
	var duringFreeview = document.getElementsByClassName('show-during-freeview');
	
	for(var duringCtr = 0; duringCtr<duringFreeview.length; duringCtr++){
		duringFreeview[duringCtr].classList.remove('is-removed');
	}
	
	
	this.triggerPreviewRequest();
	
	return this;
};

PlayerContainer.prototype.onfreeviewStart = function(){
	var toShow = document.getElementsByClassName('show-during-freeview');
	
	for(var ctr = 0; ctr<beforeFreeview.length; ctr++){
		beforeFreeview[ctr].classList.remove('is-removed');
	}

};

PlayerContainer.prototype.onfreeviewEnd = function(){
	var afterFreeview = document.getElementsByClassName('show-after-freeview');
	
	for(var ctr = 0; ctr<afterFreeview.length; ctr++){
		afterFreeview[ctr].classList.remove('is-removed');
	}
	
	var toHide = document.getElementsByClassName('hide-after-freeview');
	for(var hideCtr = 0; hideCtr < toHide.length; hideCtr++){
		toHide[hideCtr].classList.add('is-removed');
	}
	
	return this;
};

PlayerContainer.prototype.onpaymentSeen = function(){
	var toHide = document.getElementsByClassName('remove-for-payment');

	for(var hideCtr = 0; hideCtr < toHide.length; hideCtr++){
		toHide[hideCtr].classList.add('is-removed');
	}
	
	return this;
};

PlayerContainer.prototype.onafterFreeviewPayment = function(){
	var toHide = document.getElementById('pay-after-freeview');
	toHide.classList.add('is-removed');
	
	var toShow = document.getElementsByClassName('resume-or-restart');
	for(var showCtr=0; showCtr<toShow.length; showCtr++){
		toShow[showCtr].classList.remove('is-removed');
	}
	
};