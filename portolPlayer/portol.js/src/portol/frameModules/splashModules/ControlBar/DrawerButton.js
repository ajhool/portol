function DrawerButton(attrs){
	ControlButton.call(this, {
		name: "drawerButton",
		panel: null,
		elementId: "drawer-button",
		specificMiniClass: true,
	});
	
	this.addExtendedHandlers();
	
	return this;
}

DrawerButton.prototype = Object.create(ControlButton.prototype);
DrawerButton.prototype.constructor = DrawerButton;
/*
 * TODO: Call triggerPlayerBuyRequest()
*/

DrawerButton.prototype.getElement = function(){
	return this.element;
};

DrawerButton.prototype.addExtendedHandlers = function(){
	this.addHandlers();
	//this.element.addEventListener('click', this.toggleDrawer.bind(this));
	
	return this;
};