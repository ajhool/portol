function Thumbnail(attrs){
	this.element = document.getElementById('video-thumbnail');

	this.initialize(attrs).buildElement();
	
	return this;
}

Thumbnail.prototype.initialize = function(splashURL){	
	this.url = splashURL;
	
	return this;
};

Thumbnail.prototype.buildElement = function(){
	this.element.src = this.url;
	
	return this;
};

Thumbnail.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};
