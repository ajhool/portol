function Funds(attrs){
	this.element = document.getElementById('user-balance');
	this.initialize(attrs).buildElement();
	
	return this;
}

Funds.prototype.initialize = function(attrs){
	attrs = attrs || {};
	
	this.charms = attrs.userCredits || null;
	this.cents = attrs.userBits || null;
	
	return this;
};


Funds.prototype.formatMoney = function(c, d, t){
	 var n = this.charms/100;
	 c = isNaN(c = Math.abs(c)) ? 2 : c;
	 d = d || ".";
	 t = t || ",";
	 var s = n < 0 ? "-" : ""; 
	 var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "";
	 var j = (j = i.length) > 3 ? j % 3 : 0;
	 return "$" + s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
};

Funds.prototype.buildElement = function(){
	
	//formatFunds: see gruntUtils Header.
	var formatted = this.formatMoney(2, '.', ',');
	
	this.element.innerHTML = formatted;
	
	return this;
};

Funds.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};
