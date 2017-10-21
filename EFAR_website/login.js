document.addEventListener("DOMContentLoaded", function(event) { 
  //TODO: do stuff here when page loads
});

function check_username(){
	console.log("checking name");
	var username_entered = document.getElementById("username").value;
  	var password_entered = document.getElementById("password").value;

  	if(username_entered == ""){
  		alertify.error("No username! Try Again...");
  	}else{
  		database.ref("/users").once('value', function(snapshot) {
			if(snapshot.hasChild(username_entered)) {
				check_password(password_entered, username_entered);
			}else{
				alertify.error("Wrong username! Try Again...");
			}
	    });
  	}	
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
				window.location = 'test.html';
		  	}else{
		  		alertify.error("Wrong password! Try Again...");
		  	}
		});
  	}	
}