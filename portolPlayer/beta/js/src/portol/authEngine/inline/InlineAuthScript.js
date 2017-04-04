window.googleUser = null;
window.googleLoad = false;
window.onGoogleSignIn = function(gUser){
	window.googleUser = gUser;
};

window.onVSGoogleLoad = function(){
	console.log('OnVSGoogleLoad called from the window.');	
};

var onVSGoogleLoadHook = function(){
	window.onVSGoogleLoad();
};

var onGoogleFailure = function(sEvent){
		console.log('Google failure: ', sEvent);
	};

var onGoogleSignIn = function(fEvent){
	console.log('Google success: ', fEvent);
	window.onGoogleSignIn();
};

window.FBPromise = {
	hasListener: false,
	hasReturned: false,
	callback: function(){
		console.log('Premature callback. No listener. (This function will be overwritten by first listener)');
	},
	fire: function(){
		window.FBPromise.hasReturned=true;
		if(window.FBPromise.hasListener){
			window.FBPromise.callback();
		} else {
			//Using the hasReturned flag to check when the VueScape javascript loads.
			//setTimeout(function(){window.FBPromise.fire();}, 300);
			}
		},
	}
	
  window.fbAsyncInit = function() {
  	FB.Event.subscribe('auth.statusChange', function(response) {
	console.log('fb auth.statusChange', response);
	window.FBPromise.fire();
});

FB.Event.subscribe('auth.login', function(response) {
	console.log('fb auth.login', response);
	window.FBPromise.fire();
});

FB.Event.subscribe('auth.authResponseChange', function(response){
	console.log('fb auth.authRsponseChange', response);
});

FB.init({
  appId      : '1080777671932760',
  xfbml      : true,
  cookie	 : true,
  status	 : true,
  version    : 'v2.5'
	});
	
	window.FBPromise.fire();
  };
  
  (function(d, s, id){
	 var js, fjs = d.getElementsByTagName(s)[0];
	 if (d.getElementById(id)) {return;}
	 js = d.createElement(s); js.id = id;
	 js.src = "//connect.facebook.net/en_US/sdk.js";
	 fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));