function PlayerResponse(attrs) {
	this.setup(attrs);
}

PlayerResponse.prototype.setup = function(attrs){
	attrs = attrs || {};
	
	this.id = attrs.id || null;
	this.btcPaymentAddr = attrs.btcPaymentAddr || null;
	this.mpdAuthorized = attrs.mpdAuthorized || false;
	//this.mpdfile = attrs.mpdfile || null;
	this.dedicatedCloudHost = attrs.dedicatedCloudHost || null;
	this.loggedIn = new LoggedIn(attrs.loggedIn);
	this.newStatus = attrs.newStatus || 'UNINITIALIZED';
	this.playerId = attrs.playerId || null;
	this.previewMPD = attrs.previewMPD || null;
	this.previewMPDAvailable = attrs.previewMPDAvailable || null;
	this.priceInCents = attrs.priceInCents || null;
	//this.splashContents = new SplashLayer(attrs.splashContents);
	this.splashContents = attrs.splashContents || null;
	this.totReceived = attrs.totReceived || null ;
	this.totRequest = attrs.totRequest || null;
	this.type = attrs.type || null;
	this.videoKey = attrs.videoKey || null;
	
	return this;
};

PlayerResponse.prototype.getSplashContents = function(){
	console.log(this.splashContents);
	return this.splashContents;
};

PlayerResponse.prototype.hasMPD = function(type) {
	var hasMPD = false;
	switch(type) {
	case 'preview' :
		if(this.previewMPD || this.previewMPDAvailable ) {
			hasMPD = true;
		}
		break;
	case 'main' :
		//if( (this.mpdfile != null) && (this.mpdfile != "") ) {
		if( this.mpdAuthorized ) {
			hasMPD = true;
		}
		break;
	default :
		console.log('Need to specify parameter "type" in ServerReply.hasMPD(type) call.');
	}
	return hasMPD;
};