//implements triggerPress

function PlayerOptions(attrs){
	var a = attrs || {};
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.element = document.getElementById('player-options-container');
		
	return this;
}

PlayerOptions.prototype.hide = function(){
	this.element.classList.add('hidden-container');
	
	return this;
};