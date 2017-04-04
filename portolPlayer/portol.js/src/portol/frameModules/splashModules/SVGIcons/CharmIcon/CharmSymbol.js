function CharmSymbol(){
	this.symbol = document.createElementNS(portol_svgns, 'symbol');
	this.symbol.setAttribute('width', 130);
	this.symbol.setAttribute('height', 80);
	this.symbol.setAttribute('id','portol-charmSymb');
	this.symbol.setAttribute('viewBox', '0 0 130 80');
	this.symbol.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");

	this.bgRect = document.createElementNS(portol_svgns, 'rect');
	this.bgRect.setAttribute('width','130');
	this.bgRect.setAttribute('height','80');
	this.bgRect.setAttribute('fill',"#00D599");
	
	this.symbol.appendChild(this.bgRect);
	
	this.text = document.createElementNS(portol_svgns, 'text');
	this.text.setAttribute('x', '20');
	this.text.setAttribute('y', '45');
	this.text.setAttribute('font-family', 'Open Sans, Lucida Grande, Tahoma, Verdana, sans-serif');
	this.text.setAttribute('font-weight', '400');
	this.text.setAttribute('font-size', '1.2em');
	this.text.setAttribute('fill', '#FFFFFF');
	this.text.innerHTML = '0.10';
	
	this.symbol.appendChild(this.text);

	this.emblem = document.createElementNS(portol_svgns, 'use');
	this.emblem.setAttributeNS(portol_xlinkns, 'xlink:href','#portol-charmEmb');
	this.emblem.setAttribute('x', '55');
	this.emblem.setAttribute('y', '0');
	
	this.symbol.appendChild(this.emblem);
	
	return this;
}

CharmSymbol.prototype.getSymbol = function(){
	return this.symbol;
};