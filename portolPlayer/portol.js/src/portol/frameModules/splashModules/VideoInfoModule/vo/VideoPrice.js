function VideoPrice(attrs){
	this.element = document.getElementById('price-scroll');
	
	this.initialize(attrs);
	
	return this;
}

VideoPrice.prototype.initialize = function(attrs){
	//TODO: Need server to reply with different costs
	attrs = attrs || {};
	
	this.dollar = attrs.priceInCents || 0.21;
	this.dollar = this.dollar/100;
	this.btc = attrs.priceInBits || 1;
	this.charms = attrs.shardPrice || 0.02;
		
	this.buildElement().buildSVGElement();
	
	return this;
};

VideoPrice.prototype.buildElement = function(){
	this.element.innerHTML = "<ul><li>"+ this.charms+" charms</li><li>$"+this.dollar+"</li><li>"+this.btc+" btc</li></ul>";
	this.element.style.display = 'inline-block';
	
	return this;
};

VideoPrice.prototype.buildSVGElement = function(){
	var mHeight = 61;
	var mWidth = 100;
	
	var leftX = [0, mWidth, mWidth * 2];
		
	this.dollarRect = this.mSnap.rect(leftX[0], 0, mWidth, mHeight);
	this.dollarRect.attr({fill: '#555555'});
	
	this.charmRect = this.mSnap.rect(leftX[1], 0, mWidth, mHeight);
	this.charmRect.attr({fill: '#999999'});
	
	this.btcRect = this.mSnap.rect(leftX[2], 0, mWidth, mHeight);
	this.btcRect.attr({fill: '#BBBBBB'});
		
	this.viewBox = this.mSnap.rect(0, 0, mWidth, mHeight);
	this.sprite.attr({mask: this.viewBox});
	
	this.svgElement = this.viewBox.node;
	
	return this;
};


VideoPrice.prototype.getElement = function(){
	//return (this.element) ? this.element : this.buildElement().element;
	return this.svgElement;
};