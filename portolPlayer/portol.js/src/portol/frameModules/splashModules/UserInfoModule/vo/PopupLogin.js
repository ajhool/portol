function PopupLogin(attrs){
	this.elements = document.getElementsByClassName('iframe-sign-up');
	
	this.windowReference = null;
	this.initialize(attrs);
	
	this.playerid = null;
	
	return this;
}

PopupLogin.prototype.initialize = function(attrs){
	attrs = attrs || {};
	var self = this;
	
	this.playerid = attrs.playerid || null;
	
	for(var ctr=0; ctr<this.elements.length; ctr++){
		this.elements[ctr].addEventListener('click', self.doPopup.bind(self));
	}
	
	return this;
};

PopupLogin.prototype.doPopup = function(){
	var id = this.playerid;
	var reqParams = (id) ? "?playerid=" + id : "";
	//var dest = "https://www.portol.me:5555/loginOrRegister.html" + reqParams;
	var dest = "https://www.portol.me:5555/site/user.html" + reqParams;
	
	var parentHeight = parent.document.body.clientHeight;
	var popHeight = parentHeight;
	var popWidth = parent.document.body.clientWidth/2;
	
	var popTop = parentHeight/8;
	var popLeft = popWidth/2;
	
	var strWindowFeatures = "location=no,resizable=yes,scrollbars=no,status=yes,width="+popWidth+",height="+popHeight+",top="+popTop+",left="+popLeft;
	this.windowReference = windowObjectReference = window.open(dest, "Portol Secure Login", strWindowFeatures);
	
	return this;
};

PopupLogin.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};
