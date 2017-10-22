var waiting_p;

function setup() {
	waiting_p = createP('WAITING FOR EFAR...').addClass('waiting-text');;
}

var waiting_for_efar = true;

function draw(){
	/*firebase.database().ref("/emergencies/" + key + "/state").on('value', function(snapshot) {
		var waiting_for_efar = false;		
	});*/
	if(!waiting_for_efar){
		dwaiting_p.html("Done");
	}else{
		waiting_p.html("Waiting for EFAR...");
	}
	
}