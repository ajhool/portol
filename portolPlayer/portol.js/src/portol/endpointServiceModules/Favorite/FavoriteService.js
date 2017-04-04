/*Too high level to follow .setup approach*/

function FavoriteService(allStates, params){
	this.mapEvents().grantRights();
	
	this.playerState = allStates.playerState;
	this.favoriteRequest = new FavoriteRequest(this.playerState);
	this.deleteBookmark = new DeleteBookmark(this.playerState);
	
	return this;
}

FavoriteService.prototype.mapEvents = function(){
	//no downstream events
	return this;
};

FavoriteService.prototype.grantRights = function(){
	//no rights
	return this;
};

FavoriteService.prototype.onfavoriteRequest = function(attrs){
	var self = this;
	var dest = "bookmark";
		
	var onsuccess = this.processFavoriteResponse.bind(this);
	var onerror = function(error){console.log('Favorite request error.', error);};
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.favoriteRequest.makeRequest(dest, callbacks);
	
	return this;
};

FavoriteService.prototype.onbookmarkDeleteRequest = function(){
	var self = this;
	var dest = "bookmark";
	
	var onsuccess = function(status){console.log(status);};
	var onerror = function(status){console.log(status);};
	
	var callbacks = {"onsuccess": onsuccess,
			"onerror": onerror};
	
	this.deleteBookmark.makeRequest(dest, callbacks);
	
	return this;
};

FavoriteService.prototype.processFavoriteResponse = function(response){
	var self = this;
	console.log("Player favorite response" + response);
	return this;
};