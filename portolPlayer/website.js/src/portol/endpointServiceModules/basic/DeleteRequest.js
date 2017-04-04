/**
 * 
 */

function DeleteRequest(playerState, dest, callbacks) {
	this.VERSION = "v0";
	
	var self = this;
	var mCallbacks = callbacks || {};
	
	this.dest = dest || '';

	this.utils = new EndpointDeleteUtils(this.VERSION);
	this.playerState = playerState;
		
	return this;
}

DeleteRequest.prototype.makeRequest = function(dest, callbacks){
	var self = this;
	
	this.toSend = this.playerState.getStateObject(this.mask);
	
	var targetUrl = this.utils.buildUrl(dest, this.toSend);
	
	/*
	this.success = callbacks.onsuccess.bind(callbacks.onsuccess) || function(response){
		console.log('Reply from ' + self.dest + '.', response);
	};
	*/
	
	this.success = callbacks.onsuccess;

	this.error = callbacks.onerror || function(xhr, ajaxOptions, thrownError){
		console.log(xhr, ajaxOptions, thrownError);
	};

	$.ajax({
		url: targetUrl,
		type: 'DELETE',
			
		crossDomain: true,
		xhrFields: {
			withCredentials: true
  		},
  		
  		success: self.success.bind(self),
  		error: self.error.bind(self)
	});
	
	return this;
};
