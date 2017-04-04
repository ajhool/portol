function PairingSymbol(attr){
	var self = this;
	var params = attr || {};
	this.pressed = false;
	
	this.symbol = document.createElementNS(portol_svgns, 'svg');
	this.symbol.setAttribute('viewBox', '0 0 60 60');
	this.symbol.setAttribute('width', '60');
	this.symbol.setAttribute('height', '60');
	this.symbol.style.position = 'absolute';
	this.symbol.style.left = '0';
	this.symbol.style.top = '66%';
	
	this.circle = document.createElementNS(portol_svgns, 'circle');
	this.circle.setAttribute('preserveAspectRatio', 'midXmidY meet');

	this.grad = new GradPackage();
	this.symbol.appendChild(this.grad.getDefs('pairing'));
	this.symbol.appendChild(this.circle);

	this.cx = 30;
	this.cy = 30;
	this.r = 30;

	this.startAngle = 45;
	this.endAngle = -45;
	this.focalX = this.cx;
	this.focalY = 52;

	this.sigPaths = [];

	this.circle.setAttribute('cx', this.cx);
	this.circle.setAttribute('cy', this.cy);
	this.circle.setAttribute('r', this.r);
	this.circle.setAttribute('fill', 'url(#pairing-rad-gradient)');

	this.spacing = [5, 12, 20, 28, 36];
	this.colors = ["#440000", "#442200", "#336622","#118800","#00AA88"];

	this.buildBars();
	
	this.symbol.onclick = params.clickHandler || self.clickAction.bind(self);
	
	return this;
}

PairingSymbol.prototype.buildBars = function(){

	for(var i = 0; i < this.spacing.length; i++){
		this.sigPaths[i] = this.buildArc(this.spacing[i], this.colors[i]);
		
		this.symbol.appendChild(this.sigPaths[i]);
	}
	
	return this;
};

PairingSymbol.prototype.buildArcPath = function(radius){
	var startAngle = this.startAngle;
	var endAngle = this.endAngle;
	var focX = this.focalX;	
	var focY = this.focalY;
	
	var start = this.polarToCartesian(focX, focY, radius, startAngle);
    var end = this.polarToCartesian(focX, focY, radius, endAngle);

    var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";

    var d = [
        "M", start.x, start.y, 
        "A", radius, radius, 0, arcSweep, 0, end.x, end.y
    ].join(" ");
	
	return d;
};

PairingSymbol.prototype.buildArc = function(radius, color){
	var path = document.createElementNS(portol_svgns, 'path');
	var d = this.buildArcPath(radius);
	path.setAttribute('d', d);
	
	path.setAttribute('fill', "none");
	path.setAttribute('stroke', color);
	path.setAttribute('stroke-width', '4');
	
	return path;
};

PairingSymbol.prototype.polarToCartesian = function(centerX, centerY, radius, angleInDegrees) {
  var angleInRadians = (angleInDegrees-90) * Math.PI / 180.0;

  return {
    x: centerX + (radius * Math.cos(angleInRadians)),
    y: centerY + (radius * Math.sin(angleInRadians))
  };
};

PairingSymbol.prototype.describeArc = function(x, y, radius, startAngle, endAngle){

    var start = this.polarToCartesian(x, y, radius, endAngle);
    var end = this.polarToCartesian(x, y, radius, startAngle);

    var arcSweep = endAngle - startAngle <= 180 ? "0" : "1";

    var d = [
        "M", start.x, start.y, 
        "A", radius, radius, 0, arcSweep, 0, end.x, end.y
    ].join(" ");

    return d;       
};

PairingSymbol.prototype.getElement = function(){
	return this.symbol;
};


PairingSymbol.prototype.getPressed = function(){
	return this.pressed;
};

PairingSymbol.prototype.togglePressed = function(vals){
	this.setPressed(!this.pressed);
	//this.element.style.backgroundColor = (this.pressed) ? this.pressedColor : this.unpressedColor;
	
	return this;
};

PairingSymbol.prototype.setPressed = function(pressed){
	this.pressed = pressed;
	
	return this;
};

/*Events:
	@clickAction sends the new pressed of the button up the ladder.
*/
PairingSymbol.prototype.clickAction = function(ev){
	ev.stopPropagation();
	this.togglePressed();
	this.triggerControlClick({depressed: this.pressed, unpressed: !this.pressed});
	
	return this;
};
