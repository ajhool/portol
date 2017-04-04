function PreMessage(){
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.elements = document.getElementsByClassName('show-before-freeview');
	
	this.stageZeroButton = document.getElementById('stage-0-button');
	this.stageOneButton = document.getElementById('stage-1-button');
	this.stageTwoButton = document.getElementById('stage-2-button');
	
	this.stages = document.getElementsByClassName('stage');
	this.facades = document.getElementsByClassName('facade');
	
	return this;
}

PreMessage.prototype.addHandlers = function(){
	var self = this;
	
	this.stageZeroButton.addEventListener('click', self.nextStage.bind(self, 1));
	this.stageOneButton.addEventListener('click', self.nextStage.bind(self, 2));
	this.stageTwoButton.addEventListener('click', self.surveyComplete.bind(self));
	
	return this;
};

PreMessage.prototype.nextStage = function(stageNum){
	this.facades[stageNum].classList.add('is-removed');
	this.stages[stageNum].classList.remove('is-removed');
	
	return this;
};

PreMessage.prototype.surveyComplete = function(){
	var toHide = this.elements;
	var toShow = document.getElementsByClassName('show-during-freeview');
	
	for(var ctr = 0; ctr < toHide.length; ctr ++){
		toHide[ctr].classList.add('is-removed');
	}
	
	for (var showCtr = 0; showCtr < toShow.length; showCtr++){
		toShow[showCtr].classList.remove('is-removed');
	}
				
	return this;
};
