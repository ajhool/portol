/**
 * Makes a purchase request from inside the player.
 */

function PlayerBuyRequest(callbacks, params) {
  ApiRequest.call(this, callbacks);
  var p = params || {};
  
  this.message = {
		btcAddress: p.btcAddress || null,
		playerId: p.playerId || null
  };
}
PlayerBuyRequest.prototype = Object.create(ApiRequest.prototype);
PlayerBuyRequest.prototype.constructor = PlayerBuyRequest;