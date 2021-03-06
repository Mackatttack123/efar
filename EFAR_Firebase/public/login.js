function attempt_login(){
	console.log("checking name");
	var id_entered = document.getElementById("Username").value;
    var password_entered = document.getElementById("Password").value;

  	firebase.auth().signInWithEmailAndPassword(id_entered + "@email.com", password_entered).catch(function(error) {
	  // Handle Errors here.
	  var errorCode = error.code;
	  var errorMessage = error.message;
	  // ...
	  if(errorCode === 'auth/wrong-password') {
	    alert('Wrong password.');
	  }else if(errorCode === 'auth/invalid-email') {
	    alert('Invalid user.');
	  } else {
	    alert("Wrong username or password.");
	  }
	});
	firebase.auth().onAuthStateChanged(function(user) {
		if (user) {
			// User is signed in.
			user_email = user.email;
			user_name = user_email.replace('@email.com','');
			// ...
			window.location = 'dashboard.html';
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
