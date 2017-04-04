//implements triggerPress

function PlayerBuyButton(attrs){
	var a = attrs || {};
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.pressed = a.pressed || false;
	this.element = document.getElementById('player-option-buy');
	this.elements = document.getElementsByClassName('buy-now-button');
		
	return this;
}

PlayerBuyButton.prototype.addHandlers = function(){
	var self = this;
	
	for(var ctr = 0; ctr< this.elements.length; ctr++){
		this.elements[ctr].addEventListener('click', this.doBuy.bind(this));
	}
	
	var afterFreeview = document.getElementById('pay-after-freeview');
	afterFreeview.addEventListener('click', function(){
		self.triggerAfterFreeviewPayment();
	});

	return this;
};

PlayerBuyButton.prototype.doBuy = function(){
	this.triggerPlayerBuyRequest();
	return this;
};

PlayerBuyButton.prototype.hide = function(){
	this.element.classList.add('hidden-button');
	return this;
};