function EventBus(newEvents){
	var self = this;
	this.events = [];
	newEvents = [].concat(newEvents);
	
	newEvents.forEach(function(event){
			self.events.push(new Event(event));
	});
}

/* In case multiple triggers for same event is overwriting them.
EventBus.prototype.addTrigger = function(caller, eventName, args) {
	caller['trigger' + eventName] = this.trigger;
	return this;
};
*/

EventBus.prototype.trigger = function(eventName, args){
	var success = this.events.some(function(target){
		if(target.name === eventName) {
			target.fire(args);
			return true;
		}
	});
	
	if(!success){
		console.log("Triggering " + eventName + " was unsuccessful.");
	}
	
	return this;
};

EventBus.prototype.removeEvent = function(eventName){
	var self = this;
	
	var success = this.events.some(function(accused, idx){
		//TODO: does accused === event work?
		if(accused.name === eventName) {
			self.events.splice(idx, 1);
			return true;
		}
	});
	
	if(!success) {
		console.log("Could not remove event " + eventName + " from event bus.");
	}
	return this;
};

EventBus.prototype.subscribe = function(pointer, eventName){
	var self = this;
	//can subscribe to one event or many at the same time
	eventName = [].concat(eventName);
	//eventName = (eventName.constructor === Array) ? eventName : [eventName];
	eventName.forEach(function(event) {
		var success = self.events.some(function(target){
			if(target.name === event) {
				target.subscribe(pointer);
				return true;
			}
		});
		if(!success){
			console.log("Subscription to " + eventName + " was unsuccessful.");
		}
	});
	
	return this;
};

EventBus.prototype.unsubscribe = function(eventNames, pointer){
	eventNames = [].concat(eventNames);
	//eventName = (eventName.constructor === Array) ? eventName : [eventName];
	eventNames.forEach(function(eventTarget) {
		var success = this.events.some(function(target){
			if(target.name === eventTarget) {
				target.unsubscribe(pointer);
				return true;
			}
		});
	
		if(!success){
			console.log("Unsubscription of ", eventNames, " by " + pointer.name + " was unsuccessful.");
		}
	});
	
	return this;
};

