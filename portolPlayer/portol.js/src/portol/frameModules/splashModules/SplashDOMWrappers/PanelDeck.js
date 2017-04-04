function PanelDeck(panels){
	this.contains = [];
	return this;
}

PanelDeck.prototype.popOffTop = function(){
	var toPop = this.contains.pop();
	return toPop;
};

PanelDeck.prototype.popOffBottom = function(){
	var toPop = this.contains.shift();
	return toPop;
};

PanelDeck.prototype.pushToTop = function(toPush){
	this.contains.push(toPush);
	return this;
};

PanelDeck.prototype.pushToBottom = function(toPush){
	this.contains.unshift(toPush);
	return this;
};

PanelDeck.prototype.getCount = function(){
	return this.contains.length;
};

PanelDeck.prototype.getSome = function(n){
	if(typeof n != 'number' ||  n < 0) {
		return null;
	}
	var pulled = [];
	
	for(var i = 0; i < n; i++) {
		pulled.push(this.popOffTop());
	}
	
	return pulled;
};