function RatingBar(attrs){
	this.totWidth = 100;
	this.height = 20;
	
	this.start = [];
	
	this.badVal = attrs.badVal;
	this.neitherVal = attrs.neitherVal;
	this.goodVal = attrs.goodVal;
	
	this.symbol = document.createElementNS(portol_svgns, 'svg');
	this.symbol.setAttribute('id','portol-ratingBar');
	this.buildBar();
	
	this.bad = document.createElementNS(portol_svgns,'rect');
	this.neither = document.createElementNS(portol_svgns, 'rect');
	this.good = document.createElementNS(portol_svgns, 'rect');
	
	this.symbol.appendChild(this.bad);
	this.bad.setAttribute('height', '100%');
	this.bad.setAttribute('width', this.badVal);
	this.bad.setAttribute('x', 0);
	this.bad.setAttribute('y', 0);
	this.bad.setAttribute('fill', '#FF0000');
		
	this.symbol.appendChild(this.neither);
	this.neither.setAttribute('height', '100%');
	this.neither.setAttribute('width', this.neitherVal);
	this.neither.setAttribute('x', this.badVal + 1);
	this.neither.setAttribute('y', 0);
	this.neither.setAttribute('fill', '#999999');
	
	this.symbol.appendChild(this.good);
	this.good.setAttribute('height', '100%');
	this.good.setAttribute('width', this.goodVal);
	this.good.setAttribute('x', this.badVal + 1 + this.neitherVal + 1);
	this.good.setAttribute('y', 0);
	this.good.setAttribute('fill', '#00FF00');
	
	return this;
}

RatingBar.prototype.buildBar = function(attrs){
	this.symbol.setAttribute('width', this.totWidth);
	this.symbol.setAttribute('height', this.height);
	this.symbol.setAttribute('viewPort', [this.totWidth,this.height].join(" "));
	
	return this;
};

RatingBar.prototype.getSymbol = function(){
	console.log('rating bar symbol', this.symbol);
	return this.symbol;
};

RatingBar.prototype.getElement = function(){
	console.log("Called getElement for ratingBar when getSymbol should be called.");
	return this.getSymbol();
};