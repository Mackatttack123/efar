document.addEventListener("DOMContentLoaded", function(event) { 
  //TODO: do stuff here when page loads
});

function attempt_login(){
	console.log("checking name");
	var id_entered = document.getElementById("dropdownFormID").value;
    var password_entered = document.getElementById("dropdownFormPassword").value;

  	firebase.auth().signInWithEmailAndPassword(id_entered + "@email.com", password_entered).catch(function(error) {
	  // Handle Errors here.
	  var errorCode = error.code;
	  var errorMessage = error.message;
	  // ...
	  if(errorCode === 'auth/wrong-password') {
	    alert('Wrong password.');
	  }else if(errorCode === 'auth/invalid-email') {
	    alertify.error('Invalid email.');
	  } else {
	    alert(errorMessage);
	  }
	});
	firebase.auth().onAuthStateChanged(function(user) {
		if (user) {
			// User is signed in.
			user_email = user.email;
			user_name = user_email.replace('@email.com','');
			// ...
			window.location = 'dispatcherhome.html';
		} 
	});
}

function logout(){
	firebase.auth().signOut().then(function() {
  	// Sign-out successful.
  	window.location = 'index.html';
	}).catch(function(error) {
	  // An error happened.
	  alert("An error occured while trying to log you out :(");
	});
}
