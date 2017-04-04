function UserImg(attrs){
	RawImage.call(this, {
		type: attrs.type,
		description: attrs.description,
		rawData: attrs.rawData,
	});
	var a = attrs || {};
		
	this.element = document.getElementById('user-pic');
	
	this.doRender();
	
	return this;
}

UserImg.prototype = Object.create(RawImage.prototype);
UserImg.prototype.constructor = UserImg;
