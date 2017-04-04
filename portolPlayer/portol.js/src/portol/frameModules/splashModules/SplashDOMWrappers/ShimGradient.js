function ShimGradient(parentElement){
	
	/*
	 * 
	 */
	this.element = document.getElementById('shim-gradient');

	this.current = 'underlay';
	
	return this;
}

ShimGradient.prototype.freeze = function(){
	if(this.current != 'frozen'){
		this.element.classList.add('frozen');
		this.element.classList.remove('underlay');
		this.current = 'frozen';
	}
	
	return this;
};

ShimGradient.prototype.underlay = function(){
	if(this.current != 'underlay') {
		this.element.classList.add('underlay');
		this.element.classList.remove('frozen');
		this.current = 'underlay';
	}
	
	return this;
};

ShimGradient.prototype.update = function(metric){
	//TODO: DON'T SWITCH CLASS ON EVERY OBSERVATION!!!
	
	if(metric.carousel.scrollTop >= metric.carousel.height) {
		this.underlay();
	} else {
		this.freeze();
	}
	
	return this;
};

