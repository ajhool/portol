function DollarIcon(standalone, price){
	this.standalone = standalone;
	if(this.standalone){
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
	
	this.dollarSymbol = document.createElementNS(portol_svgns, 'use');
	this.dollarSymbol.setAttributeNS(portol_xlinkns, 'xlink:href', '#portol-dollarSymb');
	this.dollarSymbol.setAttribute('x', 0);
	this.dollarSymbol.setAttribute('y', 0);
	
	this.main.appendChild(this.dollarSymbol);
	
	this.main.setAttribute('id', 'portol-dollarIcon');
	
	return this;
}



DollarIcon.prototype.getSVGElement = function(){
	return this.main;
};
