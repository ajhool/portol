function PortolButton(attrs){
	ControlButton.call(this, {
		name: 'portolButton',
		panel: 'account',
		section: 'accountPage',
		elementId: 'portol-button',
		specificMiniClass: true,
	});

	this.addHandlers();
	
	return this;
}

PortolButton.prototype = Object.create(ControlButton.prototype);
PortolButton.prototype.constructor = PortolButton;

PortolButton.prototype.getElement = function(){
	return this.element;
};
