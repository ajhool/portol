/* MODIFY PLAYERSTATE IN EACH OF THE CLASSES, DON"T DRIVE WITH EVENTS
*/
function AccountState(params) {
	SynchronousState.call(this);
	var p = params || {};
	
	this.paramPool = {
		  id: p.id || null,
		  userImg: p.userImg || null,
		  userName: p.userName || null,
		  firstName: p.firstName || null,
		  email: p.email || null,
		  currentToken: p.currentToken,
		  lastName: p.lastName || null,
		  signUpDate: p.signUpDate || null,
		  lastSeen: p.lastSeen || null,
		  platforms: p.platforms || null,
		  loggedInPlatformId: p.loggedInPlatformId || null,
		  loggedInPlatformExpire: p.loggedInPlatformExpire || null,
		  funds: p.funds || null,
		  history: p.history || null,
		  bookmarked: p.bookmarked || [],
		};
}

AccountState.prototype = Object.create(SynchronousState.prototype);
AccountState.prototype.constructor = AccountState;

AccountState.prototype.initialize = function(){
	this.createEventBus(['newUser']);
	
	this.triggerNewUser = this.stateEventBus.trigger.bind(this.stateEventBus, 'newUser');
	
	return this;
};

AccountState.prototype.addPlatform = function(){
	this.triggerNewUser();
	
	return this;
};