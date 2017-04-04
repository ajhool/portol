function TextPair(code){
	this.element = document.getElementById('text-pair-code');
	this.elements = document.getElementsByClassName('pairing-code');
	this.initialize(code).buildElement();
	
	return this;
}

TextPair.prototype.initialize = function(code) {
	this.code = code || "Uhhhh";
	//this.description = attrs.authorDescription || "The one that got away.";
	
	return this;
};

TextPair.prototype.buildElement = function(){
	this.element.innerHTML = this.code;
    	
	var targets = this.elements;
	for(var ctr = 0; ctr<targets.length; ctr++){
		targets[ctr].innerHTML = this.code;
	}
	
	return this;
};

TextPair.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};