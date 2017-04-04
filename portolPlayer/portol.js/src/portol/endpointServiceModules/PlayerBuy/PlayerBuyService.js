/*Too high level to follow .setup approach*/

function PlayerBuyService(allStates, params){

	
	this.mapEvents().grantRights();
	
	this.playerState = allStates.playerState;
	this.playerBuyRequest = new PlayerBuyRequest(this.playerState);
	
	return this;
}

PlayerBuyService.prototype.mapEvents = function(){
	//no downstream events
	return this;
};

PlayerBuyService.prototype.grantRights = function(){
	//no rights
	return this;
};

PlayerBuyService.prototype.onplayerBuyRequest = function(attrs){
	var self = this;
	var dest = "playerBuy";
		
	var onsuccess = this.processPlayerBuyResponse.bind(this);
	var onerror = function(error){console.log('Player Buy request error.', error);};
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.playerBuyRequest.makeRequest(dest, callbacks);
	
	return this;
};

PlayerBuyService.prototype.processPlayerBuyResponse = function(response){
	var self = this;
	
	return this;
};