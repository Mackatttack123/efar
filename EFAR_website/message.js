var canvas;

function setup() {
    canvas = createCanvas(windowWidth*0.54, windowHeight*0.64);
    // Move the canvas so it's inside our <div id="sketch-holder">.
    canvas.parent('sketch-holder');
}

var text_scroll_offset = 0;

function draw(){
	clear();
	background(240);
	// for text scroll
	if(keyIsDown(UP_ARROW)){
    	text_scroll_offset -= 10;
    }
    if(keyIsDown(DOWN_ARROW)){
    	text_scroll_offset += 10;
    }

	firebase.database().ref("/messages").on('value', function(snapshot) {
		checkDatabase();
	});
	for (var i = messages.length - 1; i >= 0; i--) {
		textSize(20);
		if(users[messages.length - 1 - i] == user_name){
			var display_message = messages[messages.length - 1 - i];
			var display_name = users[messages.length - 1 - i] + "  ";
			fill(0,50,255,50);
			rect(textWidth(display_name) + 5, height - 73 - (35 * i) - text_scroll_offset, textWidth(display_message) + 10, 30, 20);
			fill(0);
			text(display_name + display_message, 10, height - 50 - (35 * i) - text_scroll_offset);
		}else{
			var display_message = messages[messages.length - 1 - i];
			var display_name =  "  " + users[messages.length - 1 - i];
			fill(80,80,80,50);
			rect(width - textWidth(display_name) - textWidth(display_message) - 18, height - 73 - (35 * i) - text_scroll_offset, textWidth(display_message) + 10, 30, 20);
			fill(0);
			text(display_message + display_name, width - 2 - textWidth(display_message) - textWidth(display_name) - 10, height - 50 - (35 * i) - text_scroll_offset);
		}
		fill(0);
	}
	if((height - 50 - (35 * (messages.length - 1)) - text_scroll_offset) > 30){
		text_scroll_offset += 10;
	}
	if(text_scroll_offset > 0){
		text_scroll_offset -= 10;
	}
}

function windowResized() {
  resizeCanvas(windowWidth*0.54, windowHeight*0.64);
}

var last_message = "";
var messages = [];
var users = []

function checkDatabase(){
	messages = [];
	users = []
	database.ref("/messages").once("value", function(snapshot) {
	  snapshot.forEach(function(child) {
	    messages.push(child.child("message").val());
	    users.push(child.child("user").val());
	  });
	});
}

/*
var ref = firebase.database().ref('/some/path');
var obj = {someAttribute: true};
ref.push(obj);   // Creates a new ref with a new "push key"
ref.set(obj);    // Overwrites the path
ref.update(obj); // Updates only the specified attributes 
*/

function send_message(){
	var message_to_send = document.getElementById("message").value;
	if(message_to_send != ""){
		var package = {message: message_to_send, user: user_name}
		firebase.database().ref('/messages/').push(package);
		document.getElementById("message").value = "";
	}
}

function keyPressed() {
    if(keyCode == 13) {
       send_message();
    }
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


