document.addEventListener("DOMContentLoaded", function(event) { 
  //TODO: do stuff here when page loads
});

var user_email;

function check_username(){
	console.log("checking name");
	var username_entered = document.getElementById("username").value;
  	var password_entered = document.getElementById("password").value;

  	firebase.auth().signInWithEmailAndPassword(username_entered + "@email.com", password_entered).catch(function(error) {
	  // Handle Errors here.
	  var errorCode = error.code;
	  var errorMessage = error.message;
	  // ...
	  if(errorCode === 'auth/wrong-password') {
	    alertify.error('Wrong password.');
	  }else if(errorCode === 'auth/invalid-email') {
	    alertify.error('Invalid email.');
	  } else {
	    alertify.error(errorMessage);
	  }
	});
  	firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
          // User is signed in.
          user_email = user.email;
          // ...
          window.location = 'message.html';
        } 
      });

  	/*if(username_entered == ""){
  		alertify.error("No username! Try Again...");
  	}else{
  		database.ref("/users").once('value', function(snapshot) {
			if(snapshot.hasChild(username_entered)) {
				check_password(password_entered, username_entered);
			}else{
				alertify.error("Wrong username! Try Again...");
			}
	    });
  	}*/
}

function check_password(password_entered, username_entered){
	if(password_entered == ""){
  		alertify.error("No password! Try Again...");
  	}else{
  		database.ref("/users/" + username_entered + '/name').once('value', function(snapshot) {
			if(snapshot.val() == password_entered) {
		    	alertify.success("Welcome " + username_entered + "!");
		    	firebase.auth().signInAnonymously().catch(function(error) {
				  // Handle Errors here.
				  var errorCode = error.code;
				  var errorMessage = error.message;
				  // ...
				});
				window.location = 'message.html';
		  	}else{
		  		alertify.error("Wrong password! Try Again...");
		  	}
		});
  	}	
}