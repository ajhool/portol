function PanelContainer(attrs){
	var a = attrs || {};
	
	this.name = a.name || null;
	this.panel = a.panel || null;
	this.elementId = a.elementId || null;
	
	this.element = document.getElementById(this.elementId);
}

PanelContainer.prototype.getTopPosition = function(){
	return this.element.offsetTop;
};

PanelContainer.prototype.getClientHeight = function(){
	return this.element.clientHeight;
};