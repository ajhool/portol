function PairingButton(attrs){
	ControlButton.call(this, {
		name: 'pairingButton',
		panel: 'pairing',
		section: 'pairingPage',
		elementId: 'pairing-button',
		specificMiniClass: true,
	});

	this.addHandlers();
	
	return this;
}

PairingButton.prototype = Object.create(ControlButton.prototype);
PairingButton.prototype.constructor = PairingButton;
/*
PairingButton.prototype.addHandlers = function(attrs) {
	var a = attrs || {};
	
	this.attachClickHandler();
	
	return this;
};
*/
/*
PairingButton.prototype.attachClickHandler = function() {
	var self = this;

	this.element.addEventListener('click', function(){
		self.triggerBookmarkDeleteRequest();
		self.triggerChangeFocus('pairing');
	});
	
	return this;
};
*/

/*
PairingButton.prototype.attachClickHandler = function(){
	var self = this;
	this.element.addEventListener('click', function(){
		self.triggerRotateCarousel({name: 'user'
			
		});
	});
	
	return this;
};
*/
PairingButton.prototype.getElement = function(){
	return this.element;
};

PairingButton.prototype.setPlatformColor = function(color){
	/*
	document.getElementById('pairing-button-icon').style.background = "#" + r.hostPlatform.platformColor;
	*/
	this.elementIcon.style.background = '#' + color;
	return this;
};
