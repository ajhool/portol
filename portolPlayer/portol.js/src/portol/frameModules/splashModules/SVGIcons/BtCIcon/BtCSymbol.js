function BtCSymbol(){
	this.symbol = document.createElementNS(portol_svgns, 'svg');
	this.symbol.setAttribute('id','portol-btcSymb');
	this.symbol.setAttribute('viewBox', '0 0 130 80');
	this.symbol.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");

	this.bgRect = document.createElementNS(portol_svgns, 'rect');
	this.symbol.appendChild(this.bgRect);
	this.bgRect.setAttribute('width','130');
	this.bgRect.setAttribute('height','80');
	//this.bgRect.setAttribute('fill','#008888');
	this.bgRect.setAttribute('fill','url(#portol-btcGrad)');
	
	this.text = document.createElementNS(portol_svgns, 'text');
	this.text.setAttribute('x', '20');
	this.text.setAttribute('y', '45');
	this.text.setAttribute('font-family', 'Open Sans, Lucida Grande, Tahoma, Verdana, sans-serif');
	this.text.setAttribute('font-weight', '400');
	this.text.setAttribute('font-size', '1.2em');
	this.text.setAttribute('fill', '#FFFFFF');
	this.text.setAttribute('style','color: #FFFFFF');
	this.text.innerHTML = '0.15';
	
	this.symbol.appendChild(this.text);

	this.emblem = document.createElementNS(portol_svgns, 'use');
	this.emblem.setAttributeNS(portol_xlinkns, 'xlink:href','#portol-btcEmb');
	this.emblem.setAttribute('x', '100');
	this.emblem.setAttribute('y', '20');
	this.emblem.setAttribute('transform', 'scale(0.7)');
	
	this.symbol.appendChild(this.emblem);
	
	return this;
}


BtCSymbol.prototype.getSymbol = function(){
	return this.symbol;
};
