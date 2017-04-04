/* MODIFY PLAYERSTATE IN EACH OF THE CLASSES, DON"T DRIVE WITH EVENTS
*/
function PairingState(params) {
	SynchronousState.call(this);
	var p = params || {};
	
	this.paramPool = {
		castColor: p.color || null,
		qrcodeURL: p.qrcodeURL || null,
		textPairCode: p.textPairCode || null,
		};
}

PairingState.prototype = Object.create(SynchronousState.prototype);
PairingState.prototype.constructor = PairingState;