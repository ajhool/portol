function BtCIcon(standalone){
	this.standalone = standalone;
	
	if(this.standalone) {
		this.main = document.createElementNS(portol_svgns,'svg');
		this.main.setAttribute('class', 'icon');
		this.main.setAttribute('width', 130);
		this.main.setAttribute('height', 80);
		this.main.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
	} else {
		this.main = document.createElementNS(portol_svgns, 'g');
		this.main.setAttribute('width', 130);
		this.main.setAttribute('height', 80);
		this.main.setAttribute('viewBox', '0 0 130 80');
		this.main.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
	}
	
	this.gradientDefs = new GradPackage().getDefs('portol-btcGrad');
	this.main.appendChild(this.gradientDefs);
	
	this.btcSymbol = document.createElementNS(portol_svgns, 'use');
	this.btcSymbol.setAttributeNS(portol_xlinkns, 'xlink:href', '#portol-btcSymb');
	this.btcSymbol.setAttribute('x', 0);
	this.btcSymbol.setAttribute('y', 0);
	
	this.main.appendChild(this.btcSymbol);
	this.main.setAttribute('id', 'portol-btcIcon');
	
	return this;
}

BtCIcon.prototype.getSVGElement = function(){
	return this.main;
};