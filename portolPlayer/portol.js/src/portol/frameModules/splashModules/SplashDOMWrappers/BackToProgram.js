function BackToProgram(){
	
	/*
	 * 
	 */
	this.element = document.getElementById('back-to-program');
	this.current = 'hidden-bar';
	
	return this;
}

BackToProgram.prototype.freeze = function(){
	if(this.current != 'available-bar'){
		this.element.classList.add('available-bar');
		this.element.classList.remove('hidden-bar');
		this.current = 'available-bar';
	}
	
	return this;
};

BackToProgram.prototype.underlay = function(){
	if(this.current != 'hidden-bar') {
		this.element.classList.add('hidden-bar');
		this.element.classList.remove('available-bar');
		this.current = "hidden-bar";
	}
	
	return this;
};

BackToProgram.prototype.update = function(metrics){
	if(metrics.carousel.scrollTop >= metrics.panels.pairing.offsetTop){
		this.freeze();
	} else {
		this.underlay();
	}
	
	return this;
};

