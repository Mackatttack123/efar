var waiting_p;
var key;

function setup() {
	waiting_p = createP('<h1>WAITING FOR EFAR...</h1>').addClass('waiting-text');
	key = window.location.hash.substring(1)
	console.log(key);
	button_created = false;
	alerted = false;
}

var efar_name;
var efar_id;
var efar_phone;
var home_button;
var start_chat_button;
var button_created = false;
var alerted = false;

function draw(){
	var state;
	database.ref("/emergencies/" + key).on("value", function(snapshot) {
	  state = snapshot.child("state").val();
	  efar_id = snapshot.child("responding_efar").val();
	}, function (errorObject) {
	  console.log("The read failed: " + errorObject.code);
	});
	if(state == "1"){
		if(!alerted){
			alert("EFAR has been contacted!");
			alerted = true;
		}
		database.ref("/users/" + efar_id).on("value", function(snapshot) {
		  efar_name = snapshot.child("name").val();
		  efar_phone = snapshot.child("phone").val();
		}, function (errorObject) {
		  console.log("The read failed: " + errorObject.code);
		});
		waiting_p.html("<h1><center>EFAR: " + efar_name + "<p>ID #: " + efar_id + "</p><p>Phone: " + efar_phone + "</p></center></h1>");
		start_chat_button = createButton("Start Chat").class("btn btn-primary");
		start_chat_button.mousePressed(startchat);
		start_chat_button.position(windowWidth/2-40, windowHeight/2);
		home_button.hide();
		button_created = false;
	}else if(state == "0"){
		waiting_p.html("<h1><center>Waiting for EFAR...</center></h1>");
		home_button.hide();
	}else{
		if(!button_created){
			button_created = true;
			waiting_p.html("<h1><center>The EFAR has ended the emergency.<p style='padding-top: 50px'></center></h1>");
			home_button = createButton("Return Home").class("btn btn-primary");
			home_button.mousePressed(gohome);
		}
		home_button.position(windowWidth/2-40, windowHeight/2);
	}
	
}

function gohome(){
	window.location = 'home.html';
}

function startchat(){
	window.location = 'message.html#' + key;
}
