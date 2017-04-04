/* MODIFY PLAYERSTATE IN EACH OF THE CLASSES, DON"T DRIVE WITH EVENTS
*/
function SynchronousState(params) {
	var p = params || {};
  
	//WARNING: paramPool is retrieved as a literal in retrieveState:
	//		-- if you make it a prototype, change retrieveState!
	this.paramPool = {};
	this.stateEventBus = null;
}

SynchronousState.prototype.createEventBus = function(events){
	this.stateEventBus = new EventBus(events);
	
	return this;
};

SynchronousState.prototype.updateState = function(params){
	var p = params || {};
	for (var property in p) {
		
	    if (p.hasOwnProperty(property)) {
	    	
	        if(this.paramPool.hasOwnProperty(property)){	        	
					this.paramPool[property] = p[property];
	        } else {
	        	console.log('Error: updated nonexistent property: ', property);
	        }
	    }
	}
	
	return this;
};

SynchronousState.prototype.getState = function(specificFields){	
	var ret = this.getStateObject(specificFields);
	
	return JSON.stringify(ret);
};

SynchronousState.prototype.getStateObject = function(specificFields){	
	//TODO: Looking for specificFields.hasOwnProperty, maybe?
	var ret = {};
	if(specificFields){
		for(var property in specificFields) {
			if(this.paramPool.hasOwnProperty(property)) {
				//Check if it's a primitive type or custom
				ret[property] = this.paramPool[property];
			} else {
				console.log('Error: tried to retrieve nonexistent property: ' + property);
			}
		}
	} else {
		ret = this.paramPool;
	}
	
	return ret;
};

SynchronousState.prototype.addChangeListener = function(target, callback) {
	if(!this.paramPool.hasOwnProperty[target]) {
		return undefined;
	}
	console.log("Not implemented yet.");
	
	return this;
};


/* T make child class:
ApiRequest.call(this, callbacks);

FavoriteRequest.prototype = Object.create(ApiRequest.prototype);
FavoriteRequest.prototype.constructor = FavoriteRequest;
*/