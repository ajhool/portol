function UserInfoContainer(accountState) {
	PanelContainer.call(this, {
		name: 'accountPanel',
		panel: 'account',
		elementId: 'account-panel',
	});
		
	this.accountState = accountState;
	
	return this;
}

UserInfoContainer.prototype = Object.create(PanelContainer.prototype);
UserInfoContainer.prototype.constructor = UserInfoContainer;

UserInfoContainer.prototype.displayUser = function(userParams){
	this.isLoggedIn = true;
	this.username = new Username(userParams.userName);
	
	//this.money = new Money(userParams.funds);
	console.log('userParams',userParams);
	this.funds = new Funds(userParams.funds);
	
	/*
	this.money = new Money({
		btc: "50",
		dollars: "65.00",
		charms: "2000"
	});
	*/
	//this.loginButton = new LoginButton();
	
	this.userImg = new UserImg(userParams.userImg);
	this.eventBus = new EventBus();

	this.grantRights()
		.mapEvents();
	
	this.accountButtonIcon = document.getElementById('account-button-icon');
	
	var myName = document.getElementById('account-button-label');
	myName.innerHTML = this.username.getName();
	
	var iconRaw = new RawImage(userParams.userImg);
	iconRaw.element = document.getElementById('account-button-icon-image');
	iconRaw.doRender();
	this.accountButtonIcon.appendChild(iconRaw.element);
	iconRaw.element.classList.add('control-icon');
	iconRaw.element.classList.add('normal');
	
	document.getElementById('logged-in').classList.remove('hidden-container');
	document.getElementById('login-options-container').classList.add('hidden-container');
	
	var notLoggedIns = document.getElementsByClassName('not-logged-in');
	
	for(var outCtr=0;outCtr<notLoggedIns.length;outCtr++){
		notLoggedIns[outCtr].style.display='none';
	}
	
	return this;
};

UserInfoContainer.prototype.initialize = function(){

	var userParams = this.accountState.getStateObject();
	console.log(userParams);
	if(userParams.userName !== null) {
		this.displayUser(userParams);
	} else {
		this.displayGeneric(userParams);
	}
	
	this.loginPoints = new PopupLogin();
};

UserInfoContainer.prototype.displayGeneric = function(){
	console.log('Displaying generic info.');
	
	document.getElementById('logged-in').classList.add('hidden-container');
	document.getElementById('login-options-container').classList.remove('hidden-container');
	
	var loggedIns = document.getElementsByClassName('is-logged-in');

	for(var inCtr=0;inCtr<loggedIns.length;inCtr++){
		loggedIns[inCtr].style.display='none';
	}
	
	this.loginButton = new PopupLogin();
	
	return this;
};

UserInfoContainer.prototype.onsettingsPress = function(){
	return this;
};

UserInfoContainer.prototype.grantRights = function(){
	//No rights to grant here.
	
	return this;
};

UserInfoContainer.prototype.mapEvents = function(){
	this.eventBus.subscribe(this.panel, ['controlClick']);
	
	this.accountState.stateEventBus.subscribe(this, ['newUser']);
	return this;
};

UserInfoContainer.prototype.getPanelElement = function(){
	return this.panel.getElement();
};