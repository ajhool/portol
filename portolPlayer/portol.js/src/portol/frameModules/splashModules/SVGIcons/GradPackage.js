function GradPackage(){
	this.defs = document.createElementNS(portol_svgns, 'defs');
	
	this.linGrad();
	this.radGrad();
	
	this.defs.appendChild(this.linearGradient);
	this.defs.appendChild(this.radGrad);
	
	return this;
}

GradPackage.prototype.linGrad = function(){
	this.linearGradient = document.createElementNS(portol_svgns, 'linearGradient');
  	this.linearGradient.setAttribute('x1','0%');
  	this.linearGradient.setAttribute('y1','0%');
  	this.linearGradient.setAttribute('x2','100%');
  	this.linearGradient.setAttribute('y2','100%');

	this.linStop1 = document.createElementNS(portol_svgns, 'stop');
	this.linStop1.setAttribute('offset','0%');
	this.linStop1.setAttribute('style','stop-color: #008888; stop-opacity:1');
	
	this.linStop2 = document.createElementNS(portol_svgns, 'stop');
	this.linStop2.setAttribute('offset','100%');
	this.linStop2.setAttribute('style','stop-color: #00FFFF; stop-opacity:1');
	
	this.linearGradient.appendChild(this.linStop1);
	this.linearGradient.appendChild(this.linStop2);
	
	return this;
};

GradPackage.prototype.radGrad = function(){
	this.radGrad = document.createElementNS(portol_svgns, 'radialGradient');
	this.radGrad.setAttribute('cx', '66%');
	this.radGrad.setAttribute('cy', '55%');
	
	this.rgStop1 = document.createElementNS(portol_svgns, 'stop');
	this.rgStop1.setAttribute('offset', '0%');
	this.rgStop1.setAttribute('stop-color', '#99FFFF');
	this.rgStop1.setAttribute('stop-opacity', 1);
	
	this.rgStop2 = document.createElementNS(portol_svgns, 'stop');
	this.rgStop2.setAttribute('offset', '100%');
	this.rgStop2.setAttribute('stop-color', '#008888');
	this.rgStop2.setAttribute('stop-opacity', 0.9);
	
	this.radGrad.appendChild(this.rgStop1);
	this.radGrad.appendChild(this.rgStop2);
	
	return this;
};

GradPackage.prototype.getDefs = function(id){
	this.linearGradient.setAttribute('id', id + '-lin-gradient');
	this.radGrad.setAttribute('id', id + '-rad-gradient');
	return this.defs;
};
