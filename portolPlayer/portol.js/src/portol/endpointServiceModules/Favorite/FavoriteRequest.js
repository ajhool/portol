/**
 * Makes a purchase request from inside the player.
 */

function FavoriteRequest(callbacks, params) {
  ApiRequest.call(this, callbacks);
  var p = params || {};
  
  this.message = {
		loggedIn: p.loggedIn || null,
		videoKey: p.videoKey || null,
  };
}
FavoriteRequest.prototype = Object.create(ApiRequest.prototype);
FavoriteRequest.prototype.constructor = FavoriteRequest;