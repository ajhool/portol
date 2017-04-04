function VideoDescription(info){
	this.element = document.getElementById('video-description');
	this.info = info || "(default) Wow this is an interesting video. Video description test.";
	
	this.buildElement();
	
	return this;
}

VideoDescription.prototype.buildElement = function(){
	this.element.innerHTML = this.info;
    
	return this;
};

VideoDescription.prototype.getElement = function(){
	return this.element || this.buildElement().element;
};