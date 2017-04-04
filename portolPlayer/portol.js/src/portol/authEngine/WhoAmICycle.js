 /***********************************************************
  * Let's you listen to a value change
  ***********************************************************/
 if (!Object.prototype.watch) {
		Object.defineProperty(Object.prototype, "watch", {
		  enumerable: false,
		 configurable: true,
		 writable: false,
		 value: function (prop, handler) {
			var
			  oldval = this[prop],
			 getter = function () {
				return oldval;
			},
			 setter = function (newval) {
				if (oldval !== newval) {
					handler.call(this, prop, oldval, newval);
					oldval = newval;
				}
				else { return false; }
			}
			;
			
			if (delete this[prop]) { // can't watch constants
				Object.defineProperty(this, prop, {
					get: getter,
					set: setter,
					enumerable: true,
					configurable: true
				});
			}
		}
	});
}

if (!Object.prototype.unwatch) {
	Object.defineProperty(Object.prototype, "unwatch", {
		enumerable: false,
		configurable: true,
		writable: false,
		value: function (prop) {
			var val = this[prop];
			delete this[prop]; // remove accessors
			this[prop] = val;
		}
	});
}

var WhoAmICycle = function(vueScape, socialVendors){
	var self = this;
	var mIsVSVerified = null;
	var idVendors = ['vuescape', 'facebook', 'google'];
	
	//Waiting, Confirmed, Unconfirmed
	this.decision = 'WAITING';
	
	this.updateDecision = function(value){
		if(value === 'CONFIRMED'){
			this.decision = 'CONFIRMED';
			this.ondecision(true);
		} else if (value === 'DENIED') {
			this.decison = 'DENIED';
			this.ondecision(false);
		}
	};
	
	//Vuescape can update this parameter, it means vuescape knows the user.
	var iAm = null;
	
	this.vueScape = vueScape;	
	this.fbVendor = socialVendors.fb;
	this.googleVendor = socialVendors.google;
	
	this.isWaitingOnAnybody = function(newGuy){
		var isWaiting = true;
		
		//This is a bit of a hack due to the wait the .watch() function works. It does not update the .status
		// value before making the listener's callbacks. Ideally, we would just check all three vendor's statuses
		// in this function. But because it is being called on status changes of "newGuy", newGuy's status hasn't
		// updated yet, so we have to assume it is 'DENIED';
		switch(newGuy){
			case 'fb':
				isWaiting = !('DENIED' === this.vueScape.status && 'VS_DENIED' === this.googleVendor.status); 
				break;
			case 'vs':
				isWaiting = !('VS_DENIED' === this.fbVendor.status && 'VS_DENIED' === this.googleVendor.status);
				break;
			case 'google':
				isWaiting = !('DENIED' === this.vueScape.status && 'VS_DENIED' === this.fbVendor.status);
				break;
			default:
				isWaiting = !('DENIED' === this.vueScape.status && 
				'VS_DENIED' === this.fbVendor.status && 
				'VS_DENIED' === this.googleVendor.status);
		}
		/*
		if(('DENIED' === this.vueScape.status && 
				'VS_DENIED' === this.fbVendor.status && 
				'VS_DENIED' === this.googleVendor.status)){
			isWaiting = false;
			console.log('Done waiting, no users seen.');
		} else {
			console.log('Still checking users.');
			isWaiting = true;
		}*/
		
		if(isWaiting){
			console.log('Is still waiting on id vendor');
		} else {
			console.log('Is not waiting on id vendor.');
		}
		
		return isWaiting;
	};
	
	this.vueScape.watch('status', function(field,oldVal,newVal){
		if('CONFIRMED' == newVal) {
			self.updateDecision('CONFIRMED');
		} else if('DENIED' === newVal){
			if(!self.isWaitingOnAnybody('vs')){
				self.updateDecision('DENIED');
			}
		}
	});
	
	this.fbVendor.watch('status', function(field,oldVal,newVal){
		if(self.decision !== 'CONFIRMED' && self.decision !== 'VS_CONFIRMED') {
			//if(newVal == 'VS_CONFIRMED'){
			//	self.decision = 'CONFIRMED';
			//	console.log('VS Confirmed with facebook.');
			//} else {
			//	this.processStatusUpdate(oldVal, newVal);
			//}
			switch(newVal){
				case 'VS_CONFIRMED':
					self.updateDecision('CONFIRMED');
					console.log('VS Confirmed with facebook.');
					break;
				case 'VS_DENIED':
					if(!self.isWaitingOnAnybody('fb')){
						self.updateDecision('DENIED');
					}
					break;
				default:
					this.processStatusUpdate(oldVal, newVal);
					break;
			}
			
		}
	});
	
	this.googleVendor.watch('status', function(field,oldVal,newVal){
		if(self.decision !== 'CONFIRMED' && self.decision !== 'VS_CONFIRMED') {			
			switch(newVal){
				case 'VS_CONFIRMED':
					self.updateDecision('CONFIRMED');
					break;
				case 'VS_DENIED':
					if(!self.isWaitingOnAnybody('google')){
						self.updateDecision('DENIED');
					}
					break;
				default:
					this.processStatusUpdate(oldVal, newVal);
					break;
			}
		}
	});
	
	this.runCycle = function(callbacks){
		if(callbacks){
			this.ondecision = callbacks.ondecision;
		} else {
			this.ondecision = function(){console.log('Nobody listening to decision.');};
		}
		
		this.vueScape.whoAmI();
		//this.fbVendor.whoAmI();
		//this.googleVendor.whoAmI();
	};
	
	return this;
};
