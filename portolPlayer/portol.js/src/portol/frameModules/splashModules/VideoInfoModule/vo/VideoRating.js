//TODO: Add rotten tomato or IMDB

function VideoRating(rating){
	this.element = document.getElementById('video-rating');
	
	//TODO: Parse value to number if it's not there already.
	var r = rating || {"value": 0.5, "info": "Some info"};
	this.initialize(r).buildElement();
}

VideoRating.prototype.initialize = function(rating){	
	if(rating.value > 1){
		rating.value = rating.value / 10;
	}
	
	this.value = rating.value;
	this.info = rating.info;
	
	return this;
};

VideoRating.prototype.buildElement = function(){	
	var self = this;
	this.buildStars();
	
	for(var i = 0; i < 5; i ++) {
		this.addStar(true);
	}
	
	console.log(this.element);
	
	return this;
};

VideoRating.prototype.buildStars = function(){
	//return '<span>&#9733;</span><span>&#9733;</span><span>&#9733;</span><span>&#9733;</span><span>&#9733;</span>';
};

VideoRating.prototype.addStar = function(good){
	var star = document.createElement('span');
	star.innerHTML = '&#9733';	
	star.className = 'rating-star';
	star.className = star.className + ((good) ? ' good-star' : ' bad-star');
	this.element.appendChild(star);
	
	return this;
};

VideoRating.prototype.getElement = function(){
	return (this.element) ? this.element : this.buildElement().element;
};