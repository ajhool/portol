function WatchButton(attrs){
	ControlButton.call(this, {
		name: "watchButton",
		panel: 'player',
		section: 'playerPage',
		elementId: "watch-button",
		specificMiniClass: true,
	});
	
	this.addHandlers();
	
	return this;
}

WatchButton.prototype = Object.create(ControlButton.prototype);
WatchButton.prototype.constructor = WatchButton;
/*
 * TODO: Call triggerPlayerBuyRequest()
*/

WatchButton.prototype.getElement = function(){
	return this.element;
};