function AccountInfoSymbol(attr){
	var self = this;
	var params = attr || {};
	this.pressed = false;
	
	this.radius = 30;
	this.width = this.radius*2;
	this.height = this.radius*2;
	this.start = 10;
	this.end = this.width-this.start;
	
	this.symbol = document.createElementNS(portol_svgns, 'svg');
	this.symbol.setAttribute('viewBox', '0 0 60 60');
	this.symbol.setAttribute('width', this.width);
	this.symbol.style.position = 'absolute';
	this.symbol.style.left = '0';
	this.symbol.style.top = '50%';
	
	this.grad = new GradPackage();
	this.symbol.appendChild(this.grad.getDefs('accountInfo'));

	this.symbol.setAttribute('height', this.height);
	this.circle = document.createElementNS(portol_svgns, 'circle');
	this.circle.setAttribute('preserveAspectRatio', 'midXmidY meet');
	this.circle.setAttribute('cx', this.radius);
	this.circle.setAttribute('cy', this.radius);
	this.circle.setAttribute('r', this.radius);
	//this.circle.setAttribute('fill', '#FFFFFF');
	this.circle.setAttribute('fill', 'url(#accountInfo-rad-gradient)');
	
	this.topLine = this.drawBar(this.start, this.end, this.height/3, '#008888');
	this.midLine = this.drawBar(this.start, this.end, this.height/2, '#00FF22');
	this.bottomLine = this.drawBar(this.start, this.end, 2*this.height/3,'#008888');
	
	this.symbol.appendChild(this.circle);
	this.symbol.appendChild(this.topLine);
	this.symbol.appendChild(this.midLine);
	this.symbol.appendChild(this.bottomLine);
	
	this.symbol.onclick = function(ev){this.clickAction(ev);};
	
	this.symbol.onclick = params.clickHandler || self.clickAction.bind(self);
	
	return this;
}

AccountInfoSymbol.prototype.drawBar = function(xStart, xEnd, y, color){
	var height = 7;
	var L = document.createElementNS(portol_svgns, 'rect');
	L.setAttribute('width', xEnd - xStart);
	L.setAttribute('height', height);
	L.setAttribute('x', xStart);
	L.setAttribute('y', y - (height/2));
	L.setAttribute('fill', color);
	L.setAttribute('rx', '4');
	
	return L;
};

AccountInfoSymbol.prototype.getElement = function(){
	return this.symbol;
};


AccountInfoSymbol.prototype.getPressed = function(){
	return this.pressed;
};

AccountInfoSymbol.prototype.togglePressed = function(vals){
	this.setPressed(!this.pressed);
	//this.element.style.backgroundColor = (this.pressed) ? this.pressedColor : this.unpressedColor;
	
	return this;
};

AccountInfoSymbol.prototype.setPressed = function(pressed){
	this.pressed = pressed;
	
	return this;
};

/*Events:
	@clickAction sends the new pressed of the button up the ladder.
*/
AccountInfoSymbol.prototype.clickAction = function(ev){
	console.log(ev);
	ev.stopPropagation();
	this.togglePressed();
	this.triggerControlClick({depressed: this.pressed, unpressed: !this.pressed});
	
	return this;
};