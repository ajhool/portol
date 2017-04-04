function ConnectionStatus(attrs){
	this.element = document.getElementById('connection-status');
	
	this.initialize(attrs).buildElement();
}

ConnectionStatus.prototype.initialize = function(attrs) {
	attrs = attrs || {};
	
	this.status = attrs.status || 0.50;
	
	return this;
};

ConnectionStatus.prototype.buildElement = function(){
	this.element.innerHTML = "ooooo";
	this.updateDisplay();
	
	return this;
};

ConnectionStatus.prototype.updateDisplay = function(){
	this.element.style.color = "rgb(0, "+status*100+","+(1-status)*100+")";
	return this;
};

ConnectionStatus.prototype.getElement = function(){
	return this.element || this.buildElement().element;
};

ConnectionStatus.prototype.getStatus = function(){
	return this.status;
};

ConnectionStatus.prototype.setStatus = function(status){
	this.status = status;
	this.updateDisplay();
	
	return this;
};