function PriceScroll(width, height, prices){
	this.prices = prices;
	this.width = width;
	this.height = height;
	
	this.contSVG = document.createElementNS(portol_svgns, 'svg');
	this.contSVG.setAttribute('id', 'portol-ps');
	this.contSVG.setAttribute('width', this.width);
	this.contSVG.setAttribute('height', this.height);

	this.contSVG.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
		
	this.charmIcon = new CharmIcon(false, prices.shardPrice);
	this.dollarIcon = new DollarIcon(false, prices.priceInCents);
	this.btcIcon = new BtCIcon(false, prices.priceInBits);	
	
	this.left = this.charmIcon.getSVGElement();
	this.middle = this.dollarIcon.getSVGElement();
	this.right = this.btcIcon.getSVGElement();

	this.placePanel(this.left, -1);
	this.placePanel(this.middle, 0);
	this.placePanel(this.right, 1);
	
	this.contSVG.appendChild(this.btcIcon.getSVGElement());
	this.contSVG.appendChild(this.dollarIcon.getSVGElement());
	this.contSVG.appendChild(this.charmIcon.getSVGElement());
	
	var lId = '#'+this.left.getAttribute('id');
	var mId = '#'+this.middle.getAttribute('id');
	var rId = '#'+this.right.getAttribute('id');
	
	this.element = this.contSVG;
	
	return this;
}

PriceScroll.prototype.getElement = function(){
	return this.contSVG;
};

PriceScroll.prototype.placePanel = function(panel, spot){	
	var placer = -spot * this.width;
	panel.setAttribute('transform','translate(' + placer + ', 0)');

	return this;
};

PriceScroll.prototype.smilRotation = function(){
	this.toLeftAnimation();
};

PriceScroll.prototype.toLeftAnimation = function(){
	var ani = document.createElementNS("http://www.w3.org/2000/svg","animateTransform");
	ani.setAttribute("attributeName", "transform");
	ani.setAttribute("attributeType", "xml");
	ani.setAttribute("type", "translate" );
	ani.setAttribute("from", "0 0");
	ani.setAttribute("to", -this.width + " 0");
	ani.setAttribute("begin", "0s");
	ani.setAttribute("dur", "3s");
	ani.setAttribute('fill', 'freeze');
	
	this.ani = ani;
	this.middle.appendChild(this.ani);
	
	return this;
};

PriceScroll.prototype.jumpRightAnimation = function(){
	var ani = document.createElementNS("http://www.w3.org/2000/svg","animateTransform");
	ani.setAttribute("attributeName", "transform");
	ani.setAttribute("attributeType", "xml");
	ani.setAttribute("type", "translate" );
	ani.setAttribute("from", "0 0");
	ani.setAttribute("to", -this.width + " 0");
	ani.setAttribute("begin", "0s");
	ani.setAttribute("dur", "3s");
	ani.setAttribute('fill', 'freeze');
	
	this.ani = ani;
	this.middle.appendChild(this.ani);
	
	return this;
};

PriceScroll.prototype.toCenterAnimation = function(){
	var ani = document.createElementNS("http://www.w3.org/2000/svg","animateTransform");
	ani.setAttribute("attributeName", "transform");
	ani.setAttribute("attributeType", "xml");
	ani.setAttribute("type", "translate" );
	ani.setAttribute("from", "0 0");
	ani.setAttribute("to", -this.width + " 0");
	ani.setAttribute("begin", "0s");
	ani.setAttribute("dur", "3s");
	ani.setAttribute('fill', 'freeze');
	
	this.ani = ani;
	this.middle.appendChild(this.ani);
	
	return this;
};

PriceScroll.prototype.toMiddleAnimation = function(){
	var ani = document.createElementNS("http://www.w3.org/2000/svg","animateTransform");
	ani.setAttribute("attributeName", "transform");
	ani.setAttribute("attributeType", "xml");
	ani.setAttribute("type", "translate" );
	ani.setAttribute("from", this.width + " 0");
	ani.setAttribute("to", "0 0");
	ani.setAttribute("begin", "0s");
	ani.setAttribute("dur", "3s");
	ani.setAttribute('fill', 'freeze');
	
	this.ani = ani;
	this.right.appendChild(this.ani);
	
	return this;
};

PriceScroll.prototype.bad_toLeftAnimation = function(){
	var ani = document.createElementNS("http://www.w3.org/2000/svg","animate");
	ani.setAttribute("attributeName", "x");
	ani.setAttribute("attributeType", "xml");
	//ani.setAttribute("type", "translate" );
	ani.setAttribute('begin','0s');
	ani.setAttribute("from", "0");
	ani.setAttribute('repeatCount', '1');
	var w = -this.width;
	ani.setAttribute("to",  w.toString());
	ani.setAttribute("dur", "10s");
	//ani.setAttribute('fill', 'freeze');
	
	this.ani = ani;
	this.middle.appendChild(this.ani);
	
	return this;
};

PriceScroll.prototype.bad_toMiddleAnimation = function(){
	var ani = document.createElementNS("http://www.w3.org/2000/svg","animate");
	ani.setAttribute("attributeName", "x");
	ani.setAttribute("attributeType", "xml");
	//ani.setAttribute("type", "translate" );
	ani.setAttribute('begin','0s');
	ani.setAttribute("from", this.width);
	ani.setAttribute('repeatCount', '1');
	ani.setAttribute("to", "0" );
	ani.setAttribute("dur", "3s");
	//ani.setAttribute('fill', 'freeze');
	
	this.ani = ani;
	this.right.appendChild(this.ani);
	
	return this;
};

PriceScroll.prototype.jumpRight = function(){
	this.left.setAttribute('transform','translate('+this.width+', 0)');
	
	return this;
};

PriceScroll.prototype.reassignPos = function(){
	
	var l = this.middle;
	var m = this.right;
	var r = this.left;
	
	this.left = l;
	this.middle = m;
	this.right = r;
	
	return this;
};

PriceScroll.prototype.javascriptMove = function(){
	this.toLeftAnimation()
		.toMiddleAnimation()
		.jumpRight()
		.reassignPos();
	
	return this;
};