function VideoTitle(title){
	this.element = document.getElementById('video-title');
	this.elements = document.getElementsByClassName('video-title');
	this.initialize(title).buildElement();
	
	return this;
}

VideoTitle.prototype.initialize = function(title) {	
	this.title = title || "The Titanic";
	
	return this;
};

VideoTitle.prototype.buildElement = function(){
	this.element.innerHTML = this.title;
	
	if(this.elements){
		for(var ctr=0; ctr < this.elements.length; ctr++){
			this.elements[ctr].innerHTML = this.title;
		}
	}
	return this;
};

VideoTitle.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};