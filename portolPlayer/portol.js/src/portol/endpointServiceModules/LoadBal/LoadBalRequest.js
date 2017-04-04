/**
 * Makes a request to the load balancer
 */
function LoadBalRequest(playerState, params) {
  ApiRequest.call(this, playerState);

  var p = params || {};
  
  this.message = {
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
		  status: p.status || null,
		  timeStarted: p.timeStarted || null,
		  lastReply: p.lastReply || null,
		  currentCloudPlayerId : p.currentCloudPlayerId || null,
		  userAgent: p.userAgent || null,
  };
}

LoadBalRequest.prototype = Object.create(ApiRequest.prototype);
LoadBalRequest.prototype.constructor = LoadBalRequest;