function PayWall(){
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.element = document.getElementById('mid-pay-wall');
	this.gotIt = document.getElementById('got-pre-pay-wall');

	return this;
}

PayWall.prototype.addHandlers = function(){
	var self = this;

	var targetElement = this.element;
	
	return this;
};
