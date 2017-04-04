function VideoCreator(attrs){
	this.element = document.getElementById('video-creator');
	this.elements = document.getElementsByClassName('creator-name');
	
	this.initialize(attrs).buildElement();
	
	return this;
}

VideoCreator.prototype.initialize = function(creator) {
	this.name = creator || "Steven Spielberg";
	//this.description = attrs.authorDescription || "The one that got away.";
	
	return this;
};

VideoCreator.prototype.buildElement = function(){
	this.element.innerHTML = this.name;
    
	if(this.elements){
		for(var ctr=0; ctr < this.elements.length; ctr++){
			this.elements[ctr].innerHTML = this.name;
		}
	}
	
	return this;
};

VideoCreator.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};