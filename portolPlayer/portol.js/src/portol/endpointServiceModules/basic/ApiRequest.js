/**
 * 
 */

function ApiRequest(playerState, dest, callbacks) {
	this.VERSION = "v0";
	
	var self = this;
	var mCallbacks = callbacks || {};
	
	this.dest = dest || '';

	this.utils = new EndpointUtils(this.VERSION);
	this.playerState = playerState;
		
	this.maskOn = false;
	
	return this;
}

ApiRequest.prototype.makeRequest = function(dest, callbacks){
	var self = this;
	
	if(this.maskOn) {
		var mask = this.message;
		this.toSend = this.playerState.getState(mask);
	} else {
		this.toSend = this.playerState.getState();
	}
	
	var targetUrl = this.utils.buildUrl(dest);
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
		type: 'POST',
		data: self.toSend,
		
		contentType:"application/json; charset=utf-8",
		dataType:"json",
		  
		crossDomain: true,
		xhrFields: {
			withCredentials: true
  		},
  		
  		success: self.success.bind(self),
  		error: self.error.bind(self)
	});
	
	return this;
};

ApiRequest.prototype.getMessage = function(){
	var self = this;
	console.log("Api Request: ", self);
	return JSON.stringify(self.message);
};

ApiRequest.prototype.setMessage = function(message){
	this.message = message;
	return this;
};

/*
ApiRequest.prototype.updateMessage = function(params){
	var p = params || {};
	for (var property in p) {
	    if (p.hasOwnProperty(property)) {
	        if(this.message.hasOwnProperty(property)){
	        	this.message[property] = p[property];
	        } else {
	        	console.log('Error: updated nonexistent property.');
	        }
	    }
	}
};
*/

ApiRequest.prototype.setDest = function(dest){
	this.dest = dest;
};

ApiRequest.prototype.clearMessage = function(){
	//TODO:
};

ApiRequest.prototype.destroy = function(){
	this.message = undefined;
};