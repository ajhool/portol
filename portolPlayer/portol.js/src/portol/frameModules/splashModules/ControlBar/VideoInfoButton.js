function VideoInfoButton(attrs){
	ControlButton.call(this, {
		name: 'videoInfoButton',
		panel: 'video-info',
		section: 'videoInfoPage',
		elementId: 'video-info-button',
		specificMiniClass: true,
	});

	this.addHandlers();
	
	return this;
}

VideoInfoButton.prototype = Object.create(ControlButton.prototype);
VideoInfoButton.prototype.constructor = VideoInfoButton;

VideoInfoButton.prototype.getElement = function(){
	return this.element;
};

