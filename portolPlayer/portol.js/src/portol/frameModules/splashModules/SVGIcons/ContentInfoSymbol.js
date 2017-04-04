function ContentInfoSymbol(attr){
	var self = this;
	var params = attr ||{};
	this.pressed = false;
	
	this.radius = 30;
	this.width = this.radius*2;
	this.height = this.radius*2;
	
	this.symbol = document.createElementNS(portol_svgns, 'svg');
	this.symbol.setAttribute('viewBox', '0 0 60 60');
	this.symbol.setAttribute('width', this.width);
	this.symbol.setAttribute('height', this.height);
	this.symbol.style.position = 'absolute';
	this.symbol.style.left = '0';
	this.symbol.style.top = '33%';
		
	this.grad = new GradPackage();
	this.symbol.appendChild(this.grad.getDefs('contentInfo'));
	this.backdrop = this.drawBackdrop();
	this.backdrop.setAttribute('fill', 'url(#contentInfo-rad-gradient)');
	
	this.tv = this.drawTV();
	this.antennaL = this.drawLine(19.4, 10.4, 30, 21, "#00FFFF");
	this.antennaR = this.drawLine(41.6, 10.4, 30, 21, "#0000FF");
	this.dot = this.drawDot();
	this.base = this.drawBase();
	
	this.symbol.appendChild(this.backdrop);
	this.symbol.appendChild(this.tv);
	this.symbol.appendChild(this.base);
	this.symbol.appendChild(this.dot);
	this.symbol.appendChild(this.antennaL);
	this.symbol.appendChild(this.antennaR);
	
	this.symbol.onclick = params.clickHandler || self.clickAction.bind(self);
	
	return this;
}

ContentInfoSymbol.prototype.drawBackdrop = function(){
	var B = document.createElementNS(portol_svgns, 'circle');
	B.setAttribute('preserveAspectRatio', 'midXmidY meet');
	B.setAttribute('cx', this.radius);
	B.setAttribute('cy', this.radius);
	B.setAttribute('r', this.radius);
	B.setAttribute('fill', '#FFFFFF');
	
	return B;
};

ContentInfoSymbol.prototype.drawTV = function(){
	var TV = document.createElementNS(portol_svgns, 'rect');
	TV.setAttribute('x', '10');
	TV.setAttribute('y', '21');
	TV.setAttribute('rx', '5');
	TV.setAttribute('width', '40');
	TV.setAttribute('height', '26');
	TV.setAttribute('fill', '#00FF00');
	TV.setAttribute('stroke-width', '1');
	TV.setAttribute('stroke', '#000000');
	
	return TV;
};

ContentInfoSymbol.prototype.drawLine = function(x1, y1, x2, y2, color){
	var L = document.createElementNS(portol_svgns, 'line');
	L.setAttribute('x1', x1);
	L.setAttribute('y1', y1);
	L.setAttribute('x2', x2);
	L.setAttribute('y2', y2);
	L.setAttribute('stroke', color);
	L.setAttribute('stroke-width', '2');
	
	return L;
};

ContentInfoSymbol.prototype.drawDot = function(){
	var D = document.createElementNS(portol_svgns, 'circle');
	D.setAttribute('cx', 30);
	D.setAttribute('cy', 28);
	D.setAttribute('r', 4);
	D.setAttribute('fill', '#0000FF');
	
	return D;
};

ContentInfoSymbol.prototype.drawBase = function(){
	var B = document.createElementNS(portol_svgns, 'rect');
	B.setAttribute('x', 26);
	B.setAttribute('y', 33);
	B.setAttribute('width', 8);
	B.setAttribute('height', 12);
	B.setAttribute('fill', '#0000FF');
	B.setAttribute('rx', '2');
	
	return B;
};

ContentInfoSymbol.prototype.getElement = function(){
	return this.symbol;
};


ContentInfoSymbol.prototype.getPressed = function(){
	return this.pressed;
};

ContentInfoSymbol.prototype.togglePressed = function(vals){
	this.setPressed(!this.pressed);
	//this.element.style.backgroundColor = (this.pressed) ? this.pressedColor : this.unpressedColor;
	
	return this;
};

ContentInfoSymbol.prototype.setPressed = function(pressed){
	this.pressed = pressed;
	
	return this;
};

/*Events:
	@clickAction sends the new pressed of the button up the ladder.
*/
ContentInfoSymbol.prototype.clickAction = function(ev){
	ev.stopPropagation();
	this.togglePressed();
	this.triggerControlClick({depressed: this.pressed, unpressed: !this.pressed});
	
	return this;
};