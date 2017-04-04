var VS_AuthInterface = function(){
	var vue = new VueVendor();
	var fbVendor = new FBVendor();
	var googleVendor = new GoogleVendor();
	
	fbVendor.initialize();
	googleVendor.initialize();
	
	//var whoAmICycle = new WhoAmICycle(vue, {'google': googleVendor, 'fb': fbVendor});

	this.start = function(callbacks){
		whoAmICycle.runCycle(callbacks);
	};
	
	return this;
};