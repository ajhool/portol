function CurrentToken(attrs){
	var a = attrs || {};
	
	this.value = a.value;
	this.expiration = a.expiration;
	
	return this;
}