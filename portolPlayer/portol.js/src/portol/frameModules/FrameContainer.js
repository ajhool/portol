function FrameContainer(synchronizedStates) {
	
	//this.triggerPreviewRequest = function(){console.log("Frame container original triggerPreviewRequest()")};
	this.playerState = synchronizedStates.playerState;
	this.pairingState = synchronizedStates.pairingState;
	this.accountState = synchronizedStates.accountState;
	
	//this.element = document.getElementById('portol-player');
	this.element = document.body;
	
	this.landingPage = new LandingPage();
	
	this.god = {};
	this.eventBus = new EventBus(['initSplash',
	                              'startVideo',
	                              'endpointPlay',
	                              'endpointPause',
	                              'rotateCarousel',
	                              'observableScroll']);
	
	this.shyTimeout = null;
	return this;
}

FrameContainer.prototype.oninitReady = function(){
	//var attrs = attrs || {};
	
	this.playerContainer = new PlayerContainer({
		"playerState": this.playerState,
		"accountState": this.accountState,
		"pairingState": this.pairingState,
	});
	this.playerContainer.initialize();
	
	this.splashModule = new SplashModule({
			"playerState": this.playerState,
			"accountState": this.accountState,
			"pairingState": this.pairingState
		});

	var allPanels = this.splashModule.getPanels();
	allPanels.player = this.playerContainer;
	
	this.controlBar = new ControlBar();
	this.controlBar.initialize();
	this.panelsCarousel = new PanelsCarousel(allPanels);
	
	this.landingPage.dissolve();
	
	this.mapEvents().grantRights();

	this.setupShyify();
	
	document.onmouseout = function(){
		document.getElementById('control-bar').classList.add('hidden-drawer');
		var toShy = document.getElementsByClassName('shy');
		for(var shyCtr = 0; shyCtr < toShy.length; shyCtr++){
			toShy[shyCtr].classList.add('is-hidden');
		}
		
	};
	
	document.onmousemove = function(){
		if (!document.getElementById('control-bar').classList.contains('hidden-drawer')) {
			clearTimeout(this.shyTimeout);
			this.shyTimeout = setTimeout(function() {
				document.getElementById('control-bar').classList.add('hidden-drawer');
				document.body.classList.add('hide-mouse');
				
				var toHide = document.getElementsByClassName('shy');
				for(var hideCtr=0; hideCtr<toHide.length; hideCtr++){
					toHide[hideCtr].classList.add('is-hidden');
				}
				
			}, 3000); // <-- time in milliseconds
		} else {
			document.getElementById('control-bar').classList.remove('hidden-drawer');
			document.body.classList.remove('hide-mouse');
			
			var toShow = document.getElementsByClassName('shy');
			for(var showCtr=0; showCtr<toShow.length; showCtr++){
				toShow[showCtr].classList.remove('is-hidden');
			}
		}
	};
	
	document.getElementById('control-bar').classList.remove('port-FP-uninitialized'); 
	document.getElementById('fullpage').classList.remove('port-FP-uninitialized');
	document.getElementById('control-bar').classList.add('opened-drawer'); 
	
	$('#fullpage').fullpage({
	        //Navigation
	        menu: '#control-bar',
	        lockAnchors: false,
			anchors: ['playerPage', 'videoInfoPage', 'pairingPage', 'accountPage', 'portolPage'],
	        navigation: false,
	        navigationPosition: 'right',
	        navigationTooltips: ['playerPanel', 'videoInfoPanel', 'pairingPanel'],
	        showActiveTooltip: true,
	        slidesNavigation: true,
	        slidesNavPosition: 'bottom',

	        //Scrolling
	        css3: true,
	        scrollingSpeed: 590,
	        autoScrolling: true,
	        fitToSection: true,
	        fitToSectionDelay: 400,
	        scrollBar: false,
	        easing: 'easeInOutCubic',
	        easingcss3: 'ease',
	        loopBottom: false,
	        loopTop: false,
	        loopHorizontal: true,
	        continuousVertical: false,
	        //normalScrollElements: '#element1, .element2',
	        scrollOverflow: false,
	        touchSensitivity: 5,
	        normalScrollElementTouchThreshold: 5,

	        //Accessibility
	        keyboardScrolling: true,
	        animateAnchor: true,
	        recordHistory: true,

	        //Design
	        controlArrows: true,
	        verticalCentered: true,
	        resize : false,
	        //sectionsColor : ['#ccc', '#fff'],
	        paddingTop: '3em',
	        paddingBottom: '10px',
	        fixedElements: '#shim-gradient, #theme-backdrop, #control-bar',
	        responsiveWidth: 0,
	        responsiveHeight: 0,

	        //Custom selectors
	        //sectionSelector: '.section',
	        //slideSelector: '.slide',

	        //events
	        onLeave: function(index, nextIndex, direction){
	        	
	        },
	        afterLoad: function(anchorLink, index){},
	        afterRender: function(){},
	        afterResize: function(){},
	        afterSlideLoad: function(anchorLink, index, slideAnchor, slideIndex){},
	        onSlideLeave: function(anchorLink, index, slideIndex, direction, nextSlideIndex){}
	    });
	
	//$.fn.fullpage.silentMoveTo('videoInfoPage');
	return this;
};

FrameContainer.prototype.setupShyify = function(){
	this.shyTimeout = setTimeout(function() {
		document.getElementById('control-bar').classList.add('hidden-drawer');
	}, 3000); // <-- time in milliseconds
};

FrameContainer.prototype.grantRights = function(){
	var self = this;
	
	this.playerContainer.triggerPlayerBuyRequest = function(){self.triggerPlayerBuyRequest();};
	this.playerContainer.triggerPreviewRequest = function(){self.triggerPreviewRequest();};
	
	this.splashModule.triggerPreviewRequest = function(){self.triggerPreviewRequest();};
	this.splashModule.triggerFavoriteRequest = function(){self.triggerFavoriteRequest();};
	this.splashModule.triggerBookmarkDeleteRequest = function(){self.triggerBookmarkDeleteRequest();};
	
	this.god.triggerHybrid = this.eventBus.trigger.bind(this.eventBus, 'hybrid');
	this.god.triggerInitSplash = this.eventBus.trigger.bind(this.eventBus, 'initSplash');
	this.god.triggerStartVideo = this.eventBus.trigger.bind(this.eventBus, 'startVideo');
	this.god.triggerEndpointPlay = this.eventBus.trigger.bind(this.eventBus, 'endpointPlay');
	this.god.triggerEndpointPause = this.eventBus.trigger.bind(this.eventBus, 'endpointPause');
	
	this.controlBar.triggerRotateCarousel = this.eventBus.trigger.bind(this.eventBus, 'rotateCarousel');
	this.panelsCarousel.triggerObservableScroll = this.eventBus.trigger.bind(this.eventBus, 'observableScroll');
	
	return this;
};

FrameContainer.prototype.mapEvents = function(){
	this.eventBus.subscribe(this.splashModule, ['initSplash',
	                                            'startVideo']);
	
	this.eventBus.subscribe(this.playerContainer, ['startVideo',
	                                               'endpointPlay',
	                                               'endpointPause']);
	
	this.eventBus.subscribe(this.controlBar, ['observableScroll', 'startVideo']);
	this.eventBus.subscribe(this.panelsCarousel, ['rotateCarousel', 'startVideo']);
	
	this.playerState.stateEventBus.subscribe(this, ['platformClaimed']);
	
	return this;
};

FrameContainer.prototype.onplatformClaimed = function(){
	var newColor = this.playerState.getStateObject().hostPlatform.platformColor;
	this.controlBar.pairingButton.setPlatformColor(newColor);
	return this;
};


//TODO: validateElement is not being used currently.
FrameContainer.prototype.validateElement = function() {
	var minWidth = 400;
	var minHeight = 350;
	
	var e = this.element;
	
	e.style.height = (e.clientHeight >= minHeight) ? e.style.height : minHeight;
	e.style.width = (e.clientWidth >= minWidth) ? e.style.width : minWidth;
	
	return this;
};

FrameContainer.prototype.onpreviewReady = function(previewResponse){
	//this.god.triggerPreviewReady(previewResponse);
	this.splashModule.god.triggerShyify(previewResponse);
	this.playerContainer.god.triggerFocus(previewResponse);
	
	return this;
};

FrameContainer.prototype.onvideoReady = function(videoResponse){

	this.god.triggerStartVideo(videoResponse);
	
	return this;
};

FrameContainer.prototype.onendpointPlay = function(){
	this.god.triggerEndpointPlay();
	
	return this;
};

FrameContainer.prototype.onendpointPause = function(){
	this.god.triggerEndpointPause();
	
	return this;
};

FrameContainer.prototype.onedgeWebsocketOpen = function(socket){
	this.playerContainer.addEdgeWebsocket(socket);

	return this;
};