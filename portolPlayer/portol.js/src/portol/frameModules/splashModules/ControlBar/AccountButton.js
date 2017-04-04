function AccountButton(attrs){
	ControlButton.call(this, {
		name: 'accountButton',
		panel: 'account',
		section: 'accountPage',
		elementId: 'account-button',
		specificMiniClass: true,
	});

	this.addHandlers();
	
	return this;
}

AccountButton.prototype = Object.create(ControlButton.prototype);
AccountButton.prototype.constructor = AccountButton;

AccountButton.prototype.getElement = function(){
	return this.element;
};

//account-button-icon
