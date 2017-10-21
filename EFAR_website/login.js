document.addEventListener("DOMContentLoaded", function(event) { 
  //TODO: do stuff here when page loads
});

var user_email;
var user_name;

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
          user_name = user_email.replace('@email.com','');
          // ...
          window.location = 'message.html';
        } 
    });
}
