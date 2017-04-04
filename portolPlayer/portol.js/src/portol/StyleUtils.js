function StyleUtils(){
	return this;
}

StyleUtils.prototype.noSelect = function(element) {
	element.style.webkitTouchCallout = "none";
    element.style.webkitUserSelect = "none";
    element.style.khtmlUserSelect = "none";
    element.style.mozUserSelect = "none";
    element.style.msUserSelect= "none";
    element.style.userSelect = "none";
 
 	return this;
};
 
StyleUtils.prototype.makeCircular = function(element, radius){
	element.style.height = radius + 'px';
	element.style.width = radius + 'px';
	element.style.webkitBorderRadius = radius + 'px';
	element.style.khtmlBorderRadius = radius + 'px';
	element.style.mozBorderRadius = radius + 'px';
	element.style.msBorderRadius = radius + 'px';
	element.style.borderRadius = radius + 'px';

	return this;
};