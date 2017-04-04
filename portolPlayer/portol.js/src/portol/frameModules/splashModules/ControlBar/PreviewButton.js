function PreviewButton(attrs){
	ControlButton.call(this, {
		name: 'previewButton',
		panel: 'player',
		elementId: 'preview-button',
	});
		
	this.addHandlers();
	
	return this;
}

PreviewButton.prototype = Object.create(ControlButton.prototype);
PreviewButton.prototype.constructor = PreviewButton;

/*
PreviewButton.prototype.attachClickHandler = function() {
	var self = this;

	this.element.addEventListener('click', function(){
		self.triggerPreviewRequest();
	});
	
	return this;
};
*/
/*
PreviewButton.prototype.onshyify = function(){
	this.element.style.display = 'none';
	
	return this;
};
*/
PreviewButton.prototype.getIconElement = function(){
	return this.iconElement;
};

PreviewButton.prototype.getElement = function(){
	return this.element;
};