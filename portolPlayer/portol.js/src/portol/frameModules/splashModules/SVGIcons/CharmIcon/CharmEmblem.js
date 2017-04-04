function CharmEmblem(){
	this.symbol = document.createElementNS(portol_svgns, 'symbol');
	this.symbol.setAttribute('id', 'portol-charmEmb');
	
	this.circle = document.createElementNS(portol_svgns, 'circle');
	this.circle.setAttribute('cx', 40);
	this.circle.setAttribute('cy', 40);
	this.circle.setAttribute('r', 25);
	this.circle.setAttribute('fill', 'blue');
	
	this.p1 = document.createElementNS(portol_svgns, 'path');
	this.p2 = document.createElementNS(portol_svgns, 'path');
	
	this.symbol.appendChild(this.circle);
	this.symbol.appendChild(this.p1);
	this.symbol.appendChild(this.p2);
	
	this.p1.setAttribute('d',"m 145,312 c -2,69 31,100 104,102 78,1 113,-34 109,-101 -6,-58 -62,-73 -106,-79 -48,-17 -99,-25 -99,-95 0,-48 32,-79 99,-78 60,0 97,25 96,84");
	this.p1.setAttribute('fill', '#FFFFFF');
	
	this.p2.setAttribute('d', 'm 250,15 0,470');
	this.p2.setAttribute('fill', '#FFFFFF');
	
	return this;
}

CharmEmblem.prototype.getSymbol = function(){
	return this.symbol;
};
