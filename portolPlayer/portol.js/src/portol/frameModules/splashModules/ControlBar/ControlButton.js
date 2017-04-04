//implements triggerPress

function ControlButton(attrs){
	var a = attrs || {};
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.pressed = a.pressed || false;
	this.name = a.name;
	this.panel = a.panel;
	this.mSection = a.section;
	this.elementId = a.elementId;
	this.element = document.getElementById(a.elementId);
	this.specificMiniClass = a.specificMiniClass || false;
	
	this.elementIcon = document.getElementById(a.elementId + "-icon");
	this.elementIconImage = document.getElementById(a.elementId + "-icon-image");
	this.elementLabel = document.getElementById(a.elementId + "-main");
	//this.addHandlers();
		
	return this;
}

ControlButton.prototype.addHandlers = function(){
	var self = this;
	
	this.element.addEventListener('click', self.clickAction.bind(self));
	
	return this;
};

ControlButton.prototype.getElement = function(){
	return this.element;
};

ControlButton.prototype.getPressed = function(){
	return this.pressed;
};

ControlButton.prototype.setPressed = function(pressed){
	this.pressed = pressed;
	return this;
};

ControlButton.prototype.togglePressed = function(){	
	
	this.setPressed(!this.pressed);
	return this;
};

/*Events:
	@clickAction sends the new pressed of the button up the ladder.
*/
ControlButton.prototype.clickAction = function(event){
	event.stopPropagation();
	this.togglePressed();
	/*
	this.triggerControlClick({
		depressed: this.pressed,
		unpressed: !this.pressed,
		name: this.name,
		panel: this.panel
		});
	*/
	$.fn.fullpage.moveTo(this.mSection);
	return this;
};

ControlButton.prototype.focus = function(){
	if(this.element.classList.contains('splash-button-focus')) {
		//NOOP
	} else {
		this.element.classList.add('splash-button-focus');
	}
	
    return this;
};

ControlButton.prototype.unfocus = function(){
	if(this.element.classList.contains('splash-button-focus')) {
		this.element.classList.remove('splash-button-focus');
	} else {
		//NOOP
	}
	
	return this;
};

ControlButton.prototype.toggleMini = function(){
		if(this.elementIcon){
			//this.elementIcon.classList.toggle('mini-button');
			//this.elementLabel.classList.toggle('mini-label');
			//this.element.classList.toggle('splash-button-mini');
			this.elementIcon.classList.toggle('normal');
			this.elementIcon.classList.toggle('mini');

			this.elementLabel.classList.toggle('normal');
			this.elementLabel.classList.toggle('mini');
			
			this.elementIconImage.classList.toggle('normal');
			this.elementIconImage.classList.toggle('mini');
			
			this.element.classList.toggle('normal');
			this.element.classList.toggle('mini');

		}
		return this;
};