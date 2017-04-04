function LandingPage() {
	
	//this.triggerPreviewRequest = function(){console.log("Frame container original triggerPreviewRequest()")};
	this.element = document.getElementById('landing-page');
	
	this.god = {};
	this.eventBus = new EventBus([]);
	
	//this.splashModule = new SplashModule(attrs.metaData);
	//this.playerContainer = new PlayerContainer(this.element);
	
	//this.mapEvents().grantRights();

	
	return this;
}

LandingPage.prototype.dissolve = function(){
	this.element.style.display = 'none';
	
	return this;
};