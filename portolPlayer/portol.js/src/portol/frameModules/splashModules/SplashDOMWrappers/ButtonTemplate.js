//implements triggerPress

function ButtonTemplate(params){
	params = params || {};
	/*
	 * @pressed : true (down, pressed) || false (up, unpressed)
	 */
	this.pressed = params.pressed || false;
	this.label = params.label || 'My Label';
	this.circles = params.circles || [];
	
	this.unpressedColor = "#008888";
	this.pressedColor = "#00C488";
	
	this.buildElement();
	this.buildSVGElement();
	
	return this;
}

ButtonTemplate.prototype.buildElement = function(params){
	var self = this;
	
	this.element = document.createElement('div');
	
	params = params || {label: this.label, pressed: this.pressed};
	this.label = params.label;
	this.pressed = params.pressed;
		
	var s = this.element.style;

	s.display = 'inline-block';
	s.background = (this.pressed) ? this.pressedColor : this.unpressedColor;
	s.border = 'none';
	s.width = '25%';
	s.height = '100%';
	//s.padding = '0 1.25em';
	s.fontWeight = '600';
	s.fontSize = '1.5em';
	//s.lineHeight = '1.2em';
	s.letterSpacing = '1px';
	s.textTransform = 'uppercase';
	s.color = '#FFFFFF';
	//s.position = 'relative';
	s.outline = 'none';
	s.textAlign = 'center';
	s['-webkit-tap-highlight-color'] = 'rgba(0,0,0,0)';
	s['-webkit-tap-highlight-color'] = 'transparent';
	
	s.overflow = 'hidden';
	s['-webkit-backface-visibility'] = 'hidden';
	s['-moz-backface-visibility'] = 'hidden';
	s['backface-visibility'] = 'hidden';
	
	var text = document.createElement('span');
	text.innerHTML = this.label;
	
	this.element.appendChild(text);
	this.element.onclick = params.clickHandler || self.clickAction.bind(self);
	
	pgStyleUtils.noSelect(this.element);
	
	this.addCircleSVG();
	//this.addCircles();
	
	return this;
};

ButtonTemplate.prototype.buildSVGElement = function(params){
	var self = this;
	
	params = params || {label: this.label, pressed: this.pressed};
	this.label = params.label;
	this.pressed = params.pressed;
	
	this.svgContainer = document.createElement('div');
	
	var s = this.svgContainer.style;
	s.display = 'inline-block';
	s.width = '25%';
	s.height = '100%';
	
	//this.mHeight = this.svgContainer.clientHeight;
	//this.mWidth = this.svgContainer.clientWidth;
	this.mWidth = 100;
	this.mHeight = 100;
	
	this.mSnap = Snap(this.mWidth + '%', this.mHeight + '%');
	this.rect = this.mSnap.rect(0, 0, '100%', this.mHeight + '%');
	
	this.rect.attr({
		fill: (this.pressed) ? this.pressedColor : this.unpressedColor
	});
	
	this.rect.node.onclick = params.clickHandler || self.clickAction.bind(self);
	
	this.mSnap.append(this.rect);
	
	this.svgContainer.appendChild(this.mSnap.node);
	
	this.attachIcon();
	
	pgStyleUtils.noSelect(this.svgContainer);
	
	return this;
};

ButtonTemplate.prototype.getElement = function(){
	//this.element = this.svgElement || this.element;
	return this.svgContainer || this.element;
};

ButtonTemplate.prototype.getPressed = function(){
	return this.pressed;
};

ButtonTemplate.prototype.togglePressed = function(vals){
	this.setPressed(!this.pressed);
	this.element.style.backgroundColor = (this.pressed) ? this.pressedColor : this.unpressedColor;
	
	return this;
};

ButtonTemplate.prototype.setPressed = function(pressed){
	this.pressed = pressed;
	
	return this;
};

/*Events:
	@clickAction sends the new pressed of the button up the ladder.
*/
ButtonTemplate.prototype.clickAction = function(){
	this.togglePressed();
	this.triggerControlClick({depressed: this.pressed, unpressed: !this.pressed});
	
	return this;
};

ButtonTemplate.prototype.addCircles = function(){
	//circles = [c1,c2,..]
	//c1 = {type, radius, icon || label}
	var circles = this.circles;	
	for(var ctr = 0; ctr<circles.length; ctr++){
		if(ctr === 0){
			this.element.appendChild(document.createElement('br'));
		}
		
		var c = circles[ctr];		
		var d;
		
		switch(c.type) {
		case 'img':
			d = document.createElement('img');
			d.src = c.icon;
			break;
		case 'text':
			d = document.createElement('span');
			d.innerHTML = c.label;
			break;
		}

		pgStyleUtils.makeCircular(d, c.radius);
		this.element.appendChild(d);
		
		d.onclick = c.click.bind(c);
	}
	
	return this;
};

ButtonTemplate.prototype.addCircleSVG = function(){	
	var radius = 20;
	
	var mSnap = Snap(radius*2, radius*2);
	var circle = mSnap.circle(radius, radius, radius);
	circle.node.onclick = function(e){alert("Circle Click"); e.stopPropagation();};

	mSnap.append(circle);
	
	this.element.appendChild(document.createElement('br'));
	this.element.appendChild(mSnap.node);
	
	 
	return this;
};

ButtonTemplate.prototype.attachIcon = function(){
	
	//this.icon = this.mSnap.circle(this.mWidth/2 + '%', this.mHeight/2 + '%', 10);
	this.icon = this.mSnap.circle("50%", "50%", '30%');
	this.icon.attr({
		fill : '#888888',
		stroke : '#999999',
	});
	
	this.circleText = this.mSnap.text('50%', '50%', '>');
	this.circleText.attr({stroke: '#FFFFFF'});
	
	this.mSnap.append(this.icon);
	this.mSnap.append(this.circleText);
	
	return this;
};
