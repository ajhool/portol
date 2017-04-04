function ViewCount(attrs){
	this.element = document.getElementById('view-count');
	this.initialize(attrs).buildElement();
	
	return this;
}

ViewCount.prototype.initialize = function(attrs){
	attrs = attrs || {};
	
	this.viewCount = attrs.viewCount || "(temp) 192,000";
	this.viewersCount = attrs.viewers || null;
	
	return this;
};

ViewCount.prototype.buildElement = function(){
	//Uses whichever one exists -- static vs live.
	if(this.viewCount) {
		this.element.innerHTML = this.viewCount + " views";
	} else {
		this.element.innerHTML = this.viewersCount + " viewers";
	}
		
	return this;
};

ViewCount.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};