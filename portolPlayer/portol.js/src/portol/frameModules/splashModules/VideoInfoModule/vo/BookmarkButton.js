function BookmarkButton(attrs){
	this.element = document.getElementById('bookmark-button');
		
	this.initialize(attrs);
		
	return this;
}

BookmarkButton.prototype.initialize = function(attrs) {
	attrs = attrs || {};
	
	this.attachClickHandler();
	
	return this;
};

BookmarkButton.prototype.attachClickHandler = function() {
	var self = this;

	this.element.addEventListener('click', function(){
		self.triggerBookmarkRequest();
	});
	
	return this;
};

BookmarkButton.prototype.getElement = function(){
	//return (this.element) ? this.element : this.buildElement().element;
	return this.element;
};

