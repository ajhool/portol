function ChannelInfo(attrs) {
	this.element = document.getElementById('channel-information');
	this.initialize(attrs).buildElement();
	
	return this;
}

ChannelInfo.prototype.initialize = function(attrs){
	
	attrs = attrs || {};
	
	this.channelName = new ChannelName(attrs.name);
	this.channelDescription = new ChannelDescription(attrs.description);
	
	return this;
};

ChannelInfo.prototype.buildElement = function(){
	return this;
};

ChannelInfo.prototype.getElement = function(){
	return this.element || this.buildElement().element;
};