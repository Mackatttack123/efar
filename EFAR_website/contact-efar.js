var waiting_p;
var key;

function setup() {
	waiting_p = createP('<h1>WAITING FOR EFAR...</h1>').addClass('waiting-text');
	key = window.location.hash.substring(1)
	console.log(key);
}

var waiting_for_efar = true;
var efar_name;
var efar_id;
var efar_phone;

function draw(){
	var state;
	database.ref("/emergencies/" + key).on("value", function(snapshot) {
	  state = snapshot.child("state").val();
	  efar_id = snapshot.child("responding_efar").val();
	}, function (errorObject) {
	  console.log("The read failed: " + errorObject.code);
	});
	if(state == "1"){
		waiting_for_efar = false;
	}
	if(!waiting_for_efar){
		//TODO: add an alert here!
		database.ref("/users/" + efar_id).on("value", function(snapshot) {
		  efar_name = snapshot.child("name").val();
		  efar_phone = snapshot.child("phone").val();
		}, function (errorObject) {
		  console.log("The read failed: " + errorObject.code);
		});
		waiting_p.html("<h1><center>EFAR: " + efar_name + "<p>ID #: " + efar_id + "</p><p>Phone: " + efar_phone + "</p></center></h1>");
	}else{
		waiting_p.html("<h1><center>Waiting for EFAR...</center></h1>");
	}
	
}