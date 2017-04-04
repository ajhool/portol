function PanelsCarousel(panels){
	//this.element = document.getElementById('panels-container');
	this.element = document.body;
	
	this.videoInfo = panels.videoInfo;
	this.pairing = panels.pairing;
	this.account = panels.account;
	this.portolInfo = panels.portolInfo;
	this.player = panels.player;
	
	this.shimGradient = new ShimGradient();
	this.backToProgram = new BackToProgram();

	//ajh implementation before scrollify
	//this.element.scrollTop = this.videoInfo.getTopPosition();
	//$.scrollify.instantMove('video-info-panel');
	
	//this.createObservableScroll();
}

PanelsCarousel.prototype.createObservableScroll = function(){
	this.element.addEventListener('scroll', this.announceScroll.bind(this));
	return this;
};

PanelsCarousel.prototype.observeScroll = function(){
	var self = this;
	var metric = {
			//carousel: {}, add carousel later
			panels: {
				videoInfo: {},
				pairing: {},
				account: {},
				portolInfo: {},
				player: {},
			},
			
			carousel: {}
	};

	metric.carousel.scrollTop = this.element.scrollTop;
	metric.carousel.height = this.element.clientHeight;
	metric.carousel.viewWindow = {
				"top": metric.carousel.scrollTop,
				"bottom": metric.carousel.scrollTop + metric.carousel.height,
			};
	
	
	var modules = Object.keys(metric.panels);
	var ctr = 0;
	
	for (ctr = 0; ctr < modules.length; ctr++) {
		var panelName = modules[ctr];
		var target = metric.panels[panelName];
		target.offsetTop = self[panelName].getTopPosition();
		target.clientHeight = self[panelName].getClientHeight();
		target.viewWindow = {
				"top": target.offsetTop,
				"bottom": target.offsetTop + target.clientHeight,
			};
		
		if(target.viewWindow.top >= metric.carousel.viewWindow.top &&
				target.offsetTop < metric.carousel.viewWindow.bottom){
			target.inFocus = true;
		} else {
			target.inFocus = false;
		}
	}
	
	return metric;
};

PanelsCarousel.prototype.announceScroll = function(){
	var observed = this.observeScroll();
	
	this.shimGradient.update(observed);
	this.backToProgram.update(observed);
		
	this.triggerObservableScroll(observed);
	
	return this;
};

PanelsCarousel.prototype.jumpToPanel = function(panel){
	this.element.scrollTop = panel.getElement().top;
	
	return this;
};

PanelsCarousel.prototype.rotateCarousel = function(focus){
	var self = this;
		
	if(!self.hasOwnProperty(focus)) {
		console.log("Bad carousel rotate: ", focus);
		return this;
	}
	
	var target = self[focus];
	this.startScroll(target);
	
	return this;
};

PanelsCarousel.prototype.onstartVideo = function(){
	//this.rotateCarousel('player');
	$.fn.fullpage.silentMoveTo('playerPage');
	return this;
};

PanelsCarousel.prototype.stopCurrentAnimation = function(){
	var self = this;
	
	if(this.currentAnimation){
		window.clearInterval(this.currentAnimation);
	}
	//this.element.removeEventListener('scroll', self.stopCurrentAnimation.bind(self));
	enableScroll();
	
	return this;
};

PanelsCarousel.prototype.stopAnimationOnScroll = function(){
	var self = this;
	this.element.addEventListener('scroll', self.stopCurrentAnimation.bind(self));
	
	return this;
};

PanelsCarousel.prototype.startScroll = function(target){
	this.expectedScrollPosition = this.element.scrollTop;
	
	//lock user out of scrolling
	/* before using scrollify, Aidan's scroll implementation
	disableScroll();
		
	this.stopCurrentAnimation();
	this.parallaxScroll(target);
	*/
	//Begin scrollify
	return this;
};

//TODO: Observable scrollEvents.
PanelsCarousel.prototype.parallaxScroll = function(target, carousel){
	var self = this;
	
	/*
	 * Detect a user scroll event. maintains expected position of the animation in the next frame,
	 * and if the next function call sees a different position, we know the user
	 * intervened.
	 * 
	 * The other scroll lock logic (found in gruntUtils) might be helping, but this seems
	 * to be the cure.
	 */
	var USER_SCROLL = (2 < Math.abs(this.expectedScrollPosition - this.element.scrollTop));
	var STOP_ANIMATION = false;
	
	if(USER_SCROLL || STOP_ANIMATION) {
		return;
	}
	
	var STEP_TIME = 20; //20 ms
	var SMOOTH_LANDING = 10;
	var CLOSE_ENOUGH = 2;
	
	var difference = this.element.scrollTop - target.getTopPosition();
	var sign = (difference < 0) ? -1 : 1;
	var percentChange = 10;
	
	var delta = 0;
		
	if(Math.abs(difference) < CLOSE_ENOUGH) {
		delta = null;
	} else if(Math.abs(difference) < SMOOTH_LANDING) {
		//smooth landing
		delta = 1 * sign;
	} else {
		delta = difference/percentChange;
	}
	
	if(delta) {
		this.expectedScrollPosition = this.element.scrollTop - delta;
		this.element.scrollTop = this.expectedScrollPosition;
		this.currentAnimation = window.setTimeout(self.parallaxScroll.bind(self, target), STEP_TIME);
	} else {
		STOP_ANIMATION = true;
		this.element.scrollTop = target.getTopPosition();
		this.stopCurrentAnimation();
	}
	

	return this;
};

PanelsCarousel.prototype.onrotateCarousel = function(panelName){
	this.rotateCarousel(panelName);
	return this;
};
