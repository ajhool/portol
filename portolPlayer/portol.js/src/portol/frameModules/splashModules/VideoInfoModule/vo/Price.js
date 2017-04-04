function Price(attrs){
	this.element = document.getElementById('price-scroll');
	this.elements = document.getElementsByClassName('price-info');
	this.initialize(attrs).buildElement();
	
	return this;
}

Price.prototype.initialize = function(attrs){
	attrs = attrs || {};
	
	this.btc = attrs.priceInBits || null;
	this.cents = attrs.priceInCents || null;
	this.charms = attrs.shardPrice || null;
	
	return this;
};

Price.prototype.formatMoney = function(c, d, t){
	 var n = this.cents/100;
	 c = isNaN(c = Math.abs(c)) ? 2 : c;
	 d = d || ".";
	 t = t || ",";
	 var s = n < 0 ? "-" : "";
	 var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "";
	 var j = (j = i.length) > 3 ? j % 3 : 0;
	 return "$" + s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
};

Price.prototype.buildElement = function(){
	
	//formatPrice: see gruntUtils Header.
	var formatted = this.formatMoney(2, '.', ',');
	this.element.innerHTML = formatted;

	if(this.elements){
		for(var ctr=0; ctr < this.elements.length; ctr++){
			this.elements[ctr].innerHTML = formatted;
		}
	}

	return this;
};

Price.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};
