function ChannelName(name){
	this.element = document.getElementById('channel-name');

	this.initialize(name).buildElement();
	
	return this;
}

ChannelName.prototype.initialize = function(name) {	
	this.name = name || "Great Channel Name";
	
	return this;
};

ChannelName.prototype.buildElement = function(){
	this.element.innerHTML = this.name;
    
	return this;
};

ChannelName.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};