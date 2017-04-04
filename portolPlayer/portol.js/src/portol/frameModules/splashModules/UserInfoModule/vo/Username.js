function Username(userName){
	
	this.element = document.getElementById("user-name");
	this.elements = document.getElementsByClassName('user-name');
	this.userName = userName;
	
	this.initialize();
	
	return this;
}

Username.prototype.initialize = function(){
	
	this.buildElement();
	
	return this;
};

Username.prototype.getName = function(){
	return this.userName;
};

Username.prototype.buildElement = function(){
	this.element.innerHTML = this.userName;
	
	if(this.elements){
		for(var ctr=0; ctr < this.elements.length; ctr++){
			this.elements[ctr].innerHTML = this.userName;
		}
	}
	
	return this;
};

Username.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};

