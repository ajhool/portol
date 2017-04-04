function RawImage(attrs){
	var a = attrs || {};
	
	this.type = a.type || null;
	this.rawData = a.rawData || null;
	this.description = a.description || null;
	
	return this;
}

RawImage.prototype.doRender = function(){
	this.element.src = 'data:'+this.type+';base64,'+ this.rawData;
	
	return this;
};