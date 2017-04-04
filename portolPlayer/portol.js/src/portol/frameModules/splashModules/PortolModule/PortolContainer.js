/*
 * Container element implements:
 * 
 * initialize
 * buildElement
 * show
 * hide
 * has button
 */

function PortolContainer(pairingState) {
	PanelContainer.call(this);
	
	this.element = document.getElementById('portol-panel');
		
	return this;
}

PortolContainer.prototype = Object.create(PanelContainer.prototype);
PortolContainer.prototype.constructor = PortolContainer;

PortolContainer.prototype.initialize = function(){
	this.eventBus = new EventBus(['controlClick']);
	
	this.mapEvents()
		.grantRights()
		.buildElement();
	
	return this;
};

PortolContainer.prototype.grantRights = function(){
	//No rights to grant here.
	return this;
};

PortolContainer.prototype.mapEvents = function(){
	//this.eventBus.subscribe(this.panel, ['controlClick']);
	return this;
};

PortolContainer.prototype.buildElement = function(){
	return this;
};