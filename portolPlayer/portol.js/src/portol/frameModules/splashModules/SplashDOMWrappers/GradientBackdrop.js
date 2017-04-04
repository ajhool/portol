function GradientBackdrop(parentElement){
	/*
	 * Chicken or the egg, here. Want to know the size of parentElement. 
	 * But parentElement needs this as background.
	 */
	this.element = document.createElement('canvas');
	this.parentElement = parentElement;
	this.parentElement.appendChild(this.element);
	
	return this;
}

GradientBackdrop.prototype.initialize = function(attrs){
	this.buildElement(attrs);
	
	return this;
};

GradientBackdrop.prototype.buildElement = function(attrs){
	this.color = attrs.color;
	
	var s = this.element.style;
	s.position = 'absolute';
	s.width = this.parentElement.style.width;
	s.height = this.parentElement.style.height;
	s.left = '0px';
	s.top = '0px';
	s.display = 'inline-block';
	s.backgroundSize = 'cover';
	s.backgroundRepeat = 'no-repeat';
	s.alignText = 'left';
	
	this.canvasContext = this.element.getContext('2d');
	console.log('element width', this.element.width);
	var grd=this.canvasContext.createLinearGradient(0,0,this.element.width,0);
	
	//PERSEOID GRAY
	grd.addColorStop(0, 'rgba(50,50,100,0.9)');
	grd.addColorStop(1, 'rgba(220,220,255,0.1)');
	
	//SUNRISE RED
	//grd.addColorStop(0,"rgba(238,102,85,0.901961)");
	//grd.addColorStop(1,"rgba(255,204,0,0.05)");
	
	//NIGHSTART BLUE
	//grd.addColorStop(0,'rgba(85, 102, 238,0.80)');
	//grd.addColorStop(1,'rgba(0,204,255,0.1)');
	
	//ALGAE GREEN
	//grd.addColorStop(0,'rgba(85, 200, 102,0.901961)');
	//grd.addColorStop(1,'rgba(0,255,204,0.05)');
	
	this.canvasContext.fillStyle = grd;
	console.log('element style', this.element.style.width);
	this.canvasContext.fillRect(0, 0, this.element.width, this.element.height);
	
	return this;
};

GradientBackdrop.prototype.getElement = function(){
	return this.element || this.buildElement().element;
};

GradientBackdrop.prototype.setParentBackground = function(){
	this.parentElement.style.backgroundImage = "url('" + this.element.toDataURL() + "')";

	return this;
};