/*
 * Container element implements:
 * 
 * initialize
 * buildElement
 * show
 * hide
 * has button
 */

function PairingContainer(pairingState) {
	PanelContainer.call(this, {
		name: 'pairingPanel',
		panel: 'pairing',
		elementId: 'pairing-panel',
	});
		
	this.pairingState = pairingState;
	this.qrcode = {};
	this.textPair = null;
	
	return this;
}

PairingContainer.prototype = Object.create(PanelContainer.prototype);
PairingContainer.prototype.constructor = PairingContainer;

PairingContainer.prototype.initialize = function(){
	var attrs = this.pairingState.getStateObject() || {};
	
	this.textPair = new TextPair(attrs.textPairCode);
	
	this.qrcode = new QrCode({
		url: attrs.qrcodeURL
	});
	
	
	if(('undefined' !== typeof attrs.castColor) && (null !== attrs.castColor)) {
		var cc = attrs.castColor;
		console.log(cc.length);
		
		while(cc.length < 6) {
			cc = "0" + cc;
		}
		
		console.log('color', '#'+cc);
		this.elementIconImage.style.backgroundColor = '#' + cc;
	}
	
	this.eventBus = new EventBus(['controlClick']);
	
	this.mapEvents()
		.grantRights();
	
	return this;
};

PairingContainer.prototype.grantRights = function(){
	//No rights to grant here.
	return this;
};

PairingContainer.prototype.mapEvents = function(){
	//this.panel.onclick = function
	//this.eventBus.subscribe(this.panel, ['controlClick']);
	return this;
};