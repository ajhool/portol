function ChannelDescription(attrs){
	this.element = document.getElementById('channel-description');
	this.initialize(attrs).buildElement();
	
	return this;
}

ChannelDescription.prototype.initialize = function(attrs){
	attrs = attrs || {};
	this.description = attrs.description || "Wow this is an interesting channel. Channel description test.";
	
	return this;
};

ChannelDescription.prototype.buildElement = function(){
	this.element.innerHTML = this.description;
	return this;
};

ChannelDescription.prototype.getElement = function(){
	return this.element || this.buildElement().element;
};