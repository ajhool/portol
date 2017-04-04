/**
 * Makes a purchase request from inside the player.
 */

function DeleteBookmark(callbacks, params) {
  DeleteRequest.call(this, callbacks);
  
  this.mask = {"loggedIn": true,
		  "videoKey": true};
  
}
DeleteBookmark.prototype = Object.create(DeleteRequest.prototype);
DeleteBookmark.prototype.constructor = DeleteBookmark;