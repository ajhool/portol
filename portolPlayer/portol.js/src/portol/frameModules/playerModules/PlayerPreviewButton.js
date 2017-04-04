//implements triggerPress

function PlayerPreviewButton(attrs){
	var a = attrs || {};
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.pressed = a.pressed || false;
	this.element = document.getElementById('main-play-button');
		
	return this;
}

PlayerPreviewButton.prototype.addHandlers = function(){
	var self = this;
	this.element.addEventListener('click', this.doPreview.bind(this));
	//this.element.addEventListener('click', function(){alert('hello');});

	return this;
};

PlayerPreviewButton.prototype.doPreview = function(){
	this.triggerPreviewRequest();
	return this;
};

PlayerPreviewButton.prototype.hide = function(){
	this.element.classList.add('hidden-button');
	return this;
};