/* MODIFY PLAYERSTATE IN EACH OF THE CLASSES, DON"T DRIVE WITH EVENTS
*/
function PlayerState(params) {
	SynchronousState.call(this);
	
  var p = params || {}; 
  
  //WARNING: paramPool is retrieved as a literal in retrieveState:
  //		-- if you make it a prototype, change retrieveState!
  this.paramPool = {
		  hostPlatform: p.hostPlatform || null,
		  initialConnect: p.initialConnect || null,
		  playerIP: p.playerIP || null,
		  timerExpire: p.timerExpire || null,
		  lastRequest: p.lastRequest || null,
		  numPlays: p.numPlays || null,
		  previewStatus: p.previewStatus || null,
		  referrerId: p.referrerId || null,
		  id: p.id || null,
		  playerId: p.playerId || null,
		  btcAddress: p.btcAddress || null,
		  videoKey: p.videoKey || null,
		  playerPayment: p.playerPayment || null,
		  apiKey: p.apiKey || null,
		  profile: p.profile || null,
		  numPlayersUsed: p.numPlayersUsed || null,
		  status: p.status || 'UNINITIALIZED',
		  timeStarted: p.timeStarted || null,
		  lastReply: p.lastReply || null,
		  currentCloudPlayerId : p.currentCloudPlayerId || null,
		  userAgent: p.userAgent || null,
		  mpdAuthorized: p.mpdAuthorized || null,
		  dedicatedCloudHost: p.dedicatedCloudHost || null,
		  
  };
  
  this.metaData = {};
}

PlayerState.prototype = Object.create(SynchronousState.prototype);
PlayerState.prototype.constructor = PlayerState;

PlayerState.prototype.initialize = function(){
	this.createEventBus(['platformClaimed']);
	
	this.triggerPlatformClaimed = this.stateEventBus.trigger.bind(this.stateEventBus, 'platformClaimed');
	
	return this;
};

PlayerState.prototype.addPlatform = function(){
	this.triggerPlatformClaimed();
	
	return this;
};