function Portol_SVGSymbolContainer(){
	this.contSVG = document.createElementNS(portol_svgns, 'symbol');
	document.body.appendChild(this.contSVG);

	this.contSVG.width = 100;
	this.contSVG.height = 100;
	
	this.contSVG.setAttribute('style', 'display: none;');
	this.contSVG.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
	
	var btcEmblem = new BtCEmblem();
	var dollarEmblem = new DollarEmblem();
	var charmEmblem = new CharmEmblem();
	
	this.contSVG.appendChild(btcEmblem.getSymbol());
	this.contSVG.appendChild(dollarEmblem.getSymbol());
	this.contSVG.appendChild(charmEmblem.getSymbol());

	var btcSymbol = new BtCSymbol();
	this.contSVG.appendChild(btcSymbol.getSymbol());
	
	var dollarSymbol = new DollarSymbol();
	this.contSVG.appendChild(dollarSymbol.getSymbol());
	
	var charmSymbol = new CharmSymbol();
	this.contSVG.appendChild(charmSymbol.getSymbol());	
	
	return this;
}