var canvas;

function setup() {
    canvas = createCanvas(790, 590);
    // Move the canvas so it's inside our <div id="sketch-holder">.
    canvas.parent('sketch-holder');
}

var text_scroll_offset = 0;

function draw(){
	clear();
	background(255);
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
			fill(0,100,0);
			text(users[messages.length - 1 - i] + ": " + messages[messages.length - 1 - i], 75, height - 100 - (35 * i) - text_scroll_offset);
		}else{
			fill(100,0,0);
			text(users[messages.length - 1 - i] + ": " + messages[messages.length - 1 - i], 75, height - 100 - (35 * i) - text_scroll_offset);
		}
		fill(0);
	}
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
	var package = {message: message_to_send, user: user_name}
	firebase.database().ref('/messages/').push(package);
	document.getElementById("message").value = "";
}

function keyPressed() {
    if(keyCode == 13) {
       send_message();
    }
}


