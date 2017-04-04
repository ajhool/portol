function Event(name){
	this.name = name;
	this.subscribers = [];
}

Event.prototype.subscribe = function(ptr){
	this.subscribers.push(ptr);
	return this;
};

Event.prototype.unsubscribe = function(ptr){
	var idx = this.subscribers.indexOf(ptr);
	this.subscribers.splice(idx, 1);
	
	return this;
};

Event.prototype.fire = function(args){
	var self = this;
	this.subscribers.forEach(function(target){self.action(target, args);});
	return this;
};

Event.prototype.action = function(target, args){
	var funcCall = "on" + this.name;
	if(typeof target[funcCall] != 'function') {
		console.log("Following object listening to " + this.name + " doesn't implement on" + this.name + "(). Offending object:", target);
	} else {
		target[funcCall](args);
	}
	return this;
};