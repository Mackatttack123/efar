function getInfo() {
    window.location = 'e-info.html'
}

function logout(){
	firebase.auth().signOut().then(function() {
  	// Sign-out successful.
  	window.location = 'login.html';
	}).catch(function(error) {
	  // An error happened.
	  alertify.error("An error occured while trying to log you out :(");
	});
}