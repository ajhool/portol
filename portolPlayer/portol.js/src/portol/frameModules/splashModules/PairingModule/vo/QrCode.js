function QrCode(attrs){
	this.element = document.getElementById('qrcode-img');
	
	this.url = attrs.url;
	
	this.type = attrs.type || null;
	this.rawData = attrs.rawData || null;
	this.description = attrs.description || null;
	
	this.buildElement();
	
	return this;
}

QrCode.prototype.buildElement = function(){
	if(this.url) {
		this.element.src = this.url;
	} else if(this.rawData) {
		this.element.src = 'data:'+this.type+';base64,'+ this.rawData;
	} else {
		console.log("No qr code.");
	}
	
	return this;
};

QrCode.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};
