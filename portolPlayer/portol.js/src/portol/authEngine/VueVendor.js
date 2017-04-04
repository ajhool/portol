var VueVendor = function(){
	var self = this;
	var veriStatus = {
		waiting: 'WAITING',
		confirmed: 'CONFIRMED',
		notConnected: 'NOT_CONNECTED',
		denied: 'DENIED',
		vsConfirmed: 'VS_CONFIRMED',
		vsDenied: 'VS_DENIED'
	};
	
	this.setStatus = function(status){
		if(status == veriStatus.waiting ||
			status == veriStatus.confirmed ||
			status == veriStatus.denied ||
			status == veriStatus.notConnected ||
			status == veriStatus.vsConfirmed ||
			status == veriStatus.vsDenied) {
			
			this.status = status;
		} else {
			console.log('Tried to set unknown status');
		}
	};
	
	this.status = veriStatus.waiting;
	
	this.whoAmI = function(){
		/*
		   var user = new mUserMgmt.User({
			'userName':userName,
			'password': hashed
			});
		*/

	   var r = new XMLHttpRequest();
	   //loginOrRegister is for social only
	   var to = "https://www.portol.me:5555/api/v0/user/info/me";
	   r.open("GET", to, true);
	   r.onreadystatechange = function(response) {
			switch(r.status) {
				case 200:
					//User found on this platform
					self.status = veriStatus.confirmed;
					break;
				case 402:
					//No user found on this platform, keep rolling.
					self.status = veriStatus.denied;
					break;
				default:
					console.log('Unhandled response type for whoAmI call');
					break;
			}
	   };
	   r.send();
	};
	
	this.processStatusUpdate = function(oldVal, newVal){
		if(newVal == veriStatus.confirmed) {
			
		}
	};
};
