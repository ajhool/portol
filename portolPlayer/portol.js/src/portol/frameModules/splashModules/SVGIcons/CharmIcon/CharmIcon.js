//NEED TO Post this to the dom
function CharmIcon(standalone){
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
		//this.main.setAttribute('id','portol-charmIcon');
		this.main.setAttribute('viewBox', '0 0 130 80');
		this.main.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
		
	}
	
	this.charmSymbol = document.createElementNS(portol_svgns, 'use');
	this.charmSymbol.setAttributeNS(portol_xlinkns, 'xlink:href', '#portol-charmSymb');
	this.charmSymbol.setAttribute('x', 0);
	this.charmSymbol.setAttribute('y', 0);
	
	this.main.appendChild(this.charmSymbol);
	this.main.setAttribute('id', 'portol-charmIcon');

	return this;
}

CharmIcon.prototype.getSVGElement = function(){
	return this.main;
};