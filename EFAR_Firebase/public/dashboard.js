var call_efar_layout;
var map_layout;
var all_calls_full;
var info_and_messages_my_calls;

function setup() {
	canvas = createCanvas(0, 0);
	canvas.remove();
	call_efar_layout = select("#call_efar_layout");
	map_layout = select("#map_layout");
	all_calls_full = select("#all_calls_full");
	info_and_messages_my_calls = select("#info_and_messages_my_calls");
	call_efar_layout.hide();
	info_and_messages_my_calls.hide();
    updateAllCalls();
    updateMyCurrentCalls();
    select("#data_link").hide();
    select("#personnel_link").hide();
}

function checkGrapple(){
	firebase.database().ref("/dispatchers/" + user_id).once('value', function(snapshot) {
		if(snapshot.child("grapple").exists()){
			if(snapshot.child("grapple").val().trim() === "hook"){
				select("#data_link").show();
    			select("#personnel_link").show();
			}
		}
	});
}

function updateAllCalls(){
    firebase.database().ref("/emergencies").on('value', function(emergencies_snapshot) {
    	checkGrapple();
    	if(!emergencies_snapshot.exists()){
    		var all_calls_div = document.getElementById("all_calls_div");
			all_calls_div.innerHTML = '';
    	}

		var all_calls_div = document.getElementById("all_calls_div");
		all_calls_div.innerHTML = '';

        emergencies_snapshot.forEach(function(childSnapshot) {
            var creation_date = childSnapshot.child("creation_date").val();
            var other_info = childSnapshot.child("other_info").val();
            var state = childSnapshot.child("state").val();
            var key = childSnapshot.key;

            //create div to hold emergency info
            holderDiv = createDiv("");
            holderDiv.attribute("style", "background-color: lightgrey; padding: 5px 10px 5px 10px; color: black; font-weight: 700; box-shadow: 0px 1px 3px gray; margin: 0px 2.5px 5px 2.5px;");
            holderDiv.id(key);
            holderDiv.parent("all_calls_div");

            //add address
            if(childSnapshot.hasChild("address")){
            	var address = childSnapshot.child("address").val();
            	if(address.trim() === "N/A"){
            		address_div = createDiv("Could not get address");
            	}else{
            		address_div = createDiv(address);
            	}
            }else{
            	address_div = createDiv("Could not get address");
            }
            address_div.attribute("style", "font-size: 14px;");
            address_div.parent(key);

            //add date and time
            var creation_date_array = creation_date.split("T");
            var date = creation_date_array[0];
            var time_array = creation_date_array[1].split(":");
            if(time_array[1].length == 1){
				time_array[1] = time_array[1] + "0"; 
            }

            time_div = createDiv(time_array[0] + ":" + time_array[1] + " " + date);
            time_div.attribute("style", "font-size: 12px; font-weight: 400;");
            time_div.parent(key);

            //add current call state
            if(state == "0"){
                current_state_div = createDiv("Contacting EFAR");
                current_state_div.attribute("style", "background-color: var(--efar-blue); color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
            }else if(state == "1"){
                current_state_div = createDiv("EFAR Contacted");
                current_state_div.attribute("style", "background-color: orange; color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
            }else if(state == "1.5"){
                current_state_div = createDiv("EFAR on Scene");
                current_state_div.attribute("style", "background-color: green; color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
            }else if(state == "1.75"){
	            current_state_div = createDiv("EFAR Requesting end!");
	            current_state_div.attribute("style", "background-color: red; color: white; font-size: 20px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	        }
            current_state_div.parent(key);

            //add on scene notes if possible
            if(childSnapshot.hasChild("on_scene_first_impression")){
            	var responding_efar = childSnapshot.child("responding_efar").val();
                responding_efar_list = responding_efar.split(', ');
                var on_scene_first_impression = "ERROR: Could not get on scene messsage!";
                for (var i = responding_efar_list.length - 1; i >= 0; i--) {
                	if(childSnapshot.hasChild("on_scene_first_impression/" + responding_efar_list[i])){
                		on_scene_first_impression = childSnapshot.child("on_scene_first_impression/" + responding_efar_list[i]).val().toString();
                	}
                	break;
                }
                on_scene_div_title = createDiv("ON SCENE NOTES:");
                on_scene_div_title.attribute("style", "font-size: 12px;");
                on_scene_div_title.parent(key);
                on_scene_div = createDiv(on_scene_first_impression);
                on_scene_div.attribute("style", "font-size: 12px; font-weight: 400;");
                on_scene_div.parent(key);
            }

            if(childSnapshot.hasChild("area_unsafe")){
            	var area_unsafe = childSnapshot.child("area_unsafe").val();
            	if(area_unsafe.trim() === "true"){
            		unsafe_div = createDiv("AREA UNSAFE");
	            	unsafe_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	            	unsafe_div.parent(key);
            	}else{
            		unsafe_div = createDiv("AREA SAFE");
            		unsafe_div.attribute("style", "background-color: green; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
            		unsafe_div.parent(key);
            	}

            }

            if(childSnapshot.hasChild("heart_attack")){
            	var heart_attack = childSnapshot.child("heart_attack").val();
            	if(heart_attack.trim() === "true"){
            		heart_attack_div = createDiv("HEART ATTACK");
	            	heart_attack_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	            	heart_attack_div.parent(key);
            	}
            }

            if(childSnapshot.hasChild("severe_trauma")){
            	var severe_trauma = childSnapshot.child("severe_trauma").val();
            	if(severe_trauma.trim() === "true"){
            		severe_trauma_div = createDiv("SEVERE TRAUMA");
	            	severe_trauma_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	            	severe_trauma_div.parent(key);
            	}
            }

            //TODO: add elapsed time
	        // elapsed_time_div = createDiv("<strong>ELAPSED TIME:</strong> " + "Time Here");
	        // elapsed_time_div.attribute("style", "font-size: 10px; font-weight: 400;");
	        // elapsed_time_div.parent("holderDiv" + number_of_emergencies);

       });
    });       
}

var my_calls_array = [];
var user_email;
var user_name;

function updateMyCurrentCalls(){
    firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
            // User is signed in.
            user_email = user.email;
            user_name = user_email.replace('@email.com','');

            firebase.database().ref("/dispatchers/" + user_name + "/current_calls").on('value', function(snapshot) {
            	if(!snapshot.exists()){
            		var my_current_calls_div = document.getElementById("my_current_calls_div");
					my_current_calls_div.innerHTML = '';
            	}
                my_calls_array = [];
                snapshot.forEach(function(childSnapshot) {
                  var emergency_key = childSnapshot.child("key").val();
                  var dispatcher_key = childSnapshot.key;
                  var call = {emergency_key: emergency_key, dispatcher_key: dispatcher_key, still_running: false};
                  my_calls_array.push(call);
                  setUpMyCalls();
	            });
	        });
        }
    });     
}

function setUpMyCalls(){
	firebase.database().ref("/emergencies/").on('value', function(emergencies_snapshot) {
    	var my_current_calls_div = document.getElementById("my_current_calls_div");
		my_current_calls_div.innerHTML = '';

	    emergencies_snapshot.forEach(function(childSnapshot) {
	    	for (var i = my_calls_array.length - 1; i >= 0; i--) {
	    		if(my_calls_array[i].emergency_key == childSnapshot.key){
	    			my_calls_array[i].still_running = true;
			        var creation_date = childSnapshot.child("creation_date").val();
			        var other_info = childSnapshot.child("other_info").val();
			        var state = childSnapshot.child("state").val();
			        var key = childSnapshot.key + "mycall";

			        //create div to hold emergency info
			        holderDiv = createDiv("");
			        holderDiv.attribute("style", "background-color: var(--efar-light-blue); padding: 5px 10px 5px 10px; color: black; font-weight: 700; box-shadow: 0px 1px 3px gray; margin: 0px 2.5px 5px 2.5px;");
			        holderDiv.id(key);
			        holderDiv.attribute("onclick", "window.location = '#" + key.replace("mycall", "") + "'; window.scrollTo(0,0); setUpInfoAndMessagesRight(this.id);");
			        holderDiv.parent("my_current_calls_div");

			        //add address
		            if(childSnapshot.hasChild("address")){
		            	var address = childSnapshot.child("address").val();
		            	if(address.trim() === "N/A"){
		            		address_div = createDiv("Could not get address");
		            	}else{
		            		address_div = createDiv(address);
		            	}
		            }else{
		            	address_div = createDiv("Could not get address");
		            }
		            address_div.attribute("style", "font-size: 14px;");
		            address_div.parent(key);

			        //add date and time
			        var creation_date_array = creation_date.split("T");
			        var date = creation_date_array[0];
			        var time_array = creation_date_array[1].split(":");
			        if(time_array[1].length == 1){
						time_array[1] = time_array[1] + "0"; 
			        }

			        time_div = createDiv(time_array[0] + ":" + time_array[1] + " " + date);
			        time_div.attribute("style", "font-size: 12px; font-weight: 400;");
			        time_div.parent(key);

			        //add current call state
			        if(state == "0"){
			            current_state_div = createDiv("Contacting EFAR");
			            current_state_div.attribute("style", "background-color: var(--efar-blue); color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			        }else if(state == "1"){
			            current_state_div = createDiv("EFAR Contacted");
			            current_state_div.attribute("style", "background-color: orange; color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			        }else if(state == "1.5"){
			            current_state_div = createDiv("EFAR on Scene");
			            current_state_div.attribute("style", "background-color: green; color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			        }else if(state == "1.75"){
			            current_state_div = createDiv("EFAR Requesting end!");
			            current_state_div.attribute("style", "background-color: red; color: white; font-size: 20px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			        }
			        current_state_div.parent(key);

			        //add on scene notes if possible
		            if(childSnapshot.hasChild("on_scene_first_impression")){
		            	var responding_efar = childSnapshot.child("responding_efar").val();
		                responding_efar_list = responding_efar.split(', ');
		                var on_scene_first_impression = "ERROR: Could not get on scene messsage!";
		                for (var i = responding_efar_list.length - 1; i >= 0; i--) {
		                	if(childSnapshot.hasChild("on_scene_first_impression/" + responding_efar_list[i])){
		                		on_scene_first_impression = childSnapshot.child("on_scene_first_impression/" + responding_efar_list[i]).val().toString();
		                	}
		                	break;
		                }
		                on_scene_div_title = createDiv("ON SCENE NOTES:");
		                on_scene_div_title.attribute("style", "font-size: 12px;");
		                on_scene_div_title.parent(key);
		                on_scene_div = createDiv(on_scene_first_impression);
		                on_scene_div.attribute("style", "font-size: 12px; font-weight: 400;");
		                on_scene_div.parent(key);
		            }

		            if(childSnapshot.hasChild("area_unsafe")){
		            	var area_unsafe = childSnapshot.child("area_unsafe").val();
		            	if(area_unsafe.trim() === "true"){
		            		unsafe_div = createDiv("AREA UNSAFE");
			            	unsafe_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			            	unsafe_div.parent(key);
		            	}else{
		            		unsafe_div = createDiv("AREA SAFE");
		            		unsafe_div.attribute("style", "background-color: green; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
		            		unsafe_div.parent(key);
		            	}

		            }

		            if(childSnapshot.hasChild("heart_attack")){
		            	var heart_attack = childSnapshot.child("heart_attack").val();
		            	if(heart_attack.trim() === "true"){
		            		heart_attack_div = createDiv("HEART ATTACK");
			            	heart_attack_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			            	heart_attack_div.parent(key);
		            	}
		            }

		            if(childSnapshot.hasChild("severe_trauma")){
		            	var severe_trauma = childSnapshot.child("severe_trauma").val();
		            	if(severe_trauma.trim() === "true"){
		            		severe_trauma_div = createDiv("SEVERE TRAUMA");
			            	severe_trauma_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
			            	severe_trauma_div.parent(key);
		            	}
		            }

			        //TODO: add elapsed time
			        // elapsed_time_div = createDiv("<strong>ELAPSED TIME:</strong> " + "Time Here");
			        // elapsed_time_div.attribute("style", "font-size: 10px; font-weight: 400;");
			        // elapsed_time_div.parent("holderDiv" + number_of_emergencies);
	    		}
	    	}
	    });
	    number_of_emergencies = 0;
	    cleanMyCallsOnDatabase();
	});
}

function cleanMyCallsOnDatabase(){
	for (var i = my_calls_array.length - 1; i >= 0; i--) {
		if(!my_calls_array[i].still_running){
			firebase.database().ref("/dispatchers/" + user_name + "/current_calls/" + my_calls_array[i].dispatcher_key).remove();
		}
		
	}
}

function openCallingForm(){
	map_layout.hide();
	call_efar_layout.show();
	call_efar_layout.attribute("style", "visibility: block;");
	document.getElementById("inputAddress").value = document.getElementById("map-input").value;
}

function back(){
	map_layout.show();
	call_efar_layout.hide();
}

function addEmergency() {
    //TODO: when call button is clicked, disable it and make it say loading... and disable the back button as well

    var address = document.getElementById("inputAddress").value;
    //get the lat and long for address here via google
    var geocoder = new google.maps.Geocoder();
    geocoder.geocode( { 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        var lat = results[0].geometry.location.lat();
        var long = results[0].geometry.location.lng();

        var other_info = document.getElementById("inputNotes").value;
	    var reference_number = document.getElementById("inputReferance").value;
	    var call_received_time = document.getElementById("inputCallTime").value;
	    var date = new Date();
	    var time = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "T" +  date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();

	    var package = {
	    	address: address,
	        emergency_made_by_dispatcher: user_name,
	        phone_number: "N/A",
	        reference_number: reference_number,
	        call_received_time: call_received_time,
	        other_info: other_info,
	        latitude: lat,
	        longitude: long,
	        creation_date: time,
	        state: "0"
	    }

	    key = firebase.database().ref('/emergencies/').push(package).getKey();
	    firebase.auth().onAuthStateChanged(function(user) {
	        if (user) {
	            // User is signed in.
	            var user_email = user.email;
	            var user_name = user_email.replace('@email.com','');
	            // ...
	           var dispatcher_key = firebase.database().ref('/dispatchers/' + user_name + "/current_calls").push(package).getKey();
	           firebase.database().ref('/dispatchers/' + user_name + "/current_calls/" + dispatcher_key + "/key").set(key) ; 
	        } 
	    });

	    document.getElementById("inputNotes").value = "";
	   	document.getElementById("inputReferance").value = "";
	    document.getElementById("inputCallTime").value = "";
	    document.getElementById("inputAddress").value = "";


	    map_layout.show();
		call_efar_layout.hide();

      }else{
        alert("ERROR: Invalid Address Data.");
      }
    });
    //reset the map
    initAutocomplete();
}

function setUpInfoAndMessagesRight(id){
	if(messagingOpen){
		closeInfoAndMessage();
	}else{
		messagingOpen = true;
		updateMessages();
		info_and_messages_my_calls.show();
		info_and_messages_my_calls.attribute("style", "visibility: block;");
		all_calls_full.hide();
		var id = id.replace("mycall", "");
		firebase.database().ref("/emergencies/" + id).on('value', function(emergency_snapshot) {
			var info_field = select("#info_field");
			info_field.html("");
			var creation_date = emergency_snapshot.child("creation_date").val();
	        var other_info = emergency_snapshot.child("other_info").val();
	        var state = emergency_snapshot.child("state").val();

	        //add address
	        if(emergency_snapshot.hasChild("address")){
	        	var address = emergency_snapshot.child("address").val();
	        	if(address.trim() === "N/A"){
	        		address_div = createDiv("Could not get address");
	        		document.getElementById("titleAddress").innerHTML = "Emergecy";
	        	}else{
	        		address_div = createDiv("<strong>" + address + "</strong>");
	        		document.getElementById("titleAddress").innerHTML = address; 
	        		document.getElementById("map-input").value = address;
	        		goToAddress(address, false);
	        	}
	        }else{
	        	address_div = createDiv("Could not get address");
	        }
	        address_div.attribute("style", "font-size: 14px;");
	        address_div.parent("#info_field");

	        //add date and time
	        var creation_date_array = creation_date.split("T");
	        var date = creation_date_array[0];
	        var time_array = creation_date_array[1].split(":");
	        if(time_array[1].length == 1){
				time_array[1] = time_array[1] + "0"; 
	        }

	        time_div = createDiv(time_array[0] + ":" + time_array[1] + " " + date);
	        time_div.attribute("style", "font-size: 12px; font-weight: 400;");
	        time_div.parent("#info_field");

	        //add current call state
	        if(state == "0"){
	            current_state_div = createDiv("Contacting EFAR");
	            current_state_div.attribute("style", "background-color: var(--efar-blue); color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	        }else if(state == "1"){
	            current_state_div = createDiv("EFAR Contacted");
	            current_state_div.attribute("style", "background-color: orange; color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	        }else if(state == "1.5"){
	            current_state_div = createDiv("EFAR on Scene");
	            current_state_div.attribute("style", "background-color: green; color: white; font-size: 16px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	        }else if(state == "1.75"){
	            current_state_div = createDiv("EFAR Requesting end!");
	            current_state_div.attribute("style", "background-color: red; color: white; font-size: 20px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	        }
	        current_state_div.parent("#info_field");

	        if(state == "1.75"){
	        	endButton = createButton("End Emergency");
		        endButton.attribute("class", "btn btn-secondary");
		        endButton.attribute("style", "color: white");
			    endButton.parent("#info_field");
			    endButton.attribute("onclick", "endEmergency(id);");
	        }

	        //add on scene notes if possible
            if(emergency_snapshot.hasChild("on_scene_first_impression")){
            	var responding_efar = emergency_snapshot.child("responding_efar").val();
                responding_efar_list = responding_efar.split(', ');
                var on_scene_first_impression = "ERROR: Could not get on scene messsage!";
                for (var i = responding_efar_list.length - 1; i >= 0; i--) {
                	if(emergency_snapshot.hasChild("on_scene_first_impression/" + responding_efar_list[i])){
                		on_scene_first_impression = emergency_snapshot.child("on_scene_first_impression/" + responding_efar_list[i]).val().toString();
                	}
                	break;
                }
                on_scene_div_title = createDiv("<strong>ON SCENE NOTES:</strong>");
                on_scene_div_title.attribute("style", "font-size: 12px;");
                on_scene_div_title.parent("#info_field");
                on_scene_div = createDiv(on_scene_first_impression);
                on_scene_div.attribute("style", "font-size: 12px; font-weight: 400;");
                on_scene_div.parent("#info_field");
            }

            if(emergency_snapshot.hasChild("area_unsafe")){
            	var area_unsafe = emergency_snapshot.child("area_unsafe").val();
            	if(area_unsafe.trim() === "true"){
            		unsafe_div = createDiv("AREA UNSAFE");
	            	unsafe_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	            	unsafe_div.parent("#info_field");
            	}else{
            		unsafe_div = createDiv("AREA SAFE");
            		unsafe_div.attribute("style", "background-color: green; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
            		unsafe_div.parent("#info_field");
            	}

            }

            if(emergency_snapshot.hasChild("heart_attack")){
            	var heart_attack = emergency_snapshot.child("heart_attack").val();
            	if(heart_attack.trim() === "true"){
            		heart_attack_div = createDiv("HEART ATTACK");
	            	heart_attack_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	            	heart_attack_div.parent("#info_field");
            	}
            }

            if(emergency_snapshot.hasChild("severe_trauma")){
            	var severe_trauma = emergency_snapshot.child("severe_trauma").val();
            	if(severe_trauma.trim() === "true"){
            		severe_trauma_div = createDiv("SEVERE TRAUMA");
	            	severe_trauma_div.attribute("style", "background-color: red; color: white; font-size: 12px; display: table; padding: 0px 4px 0px 4px; border-radius: 2px; margin-bottom: 4px;");
	            	severe_trauma_div.parent("#info_field");
            	}
            }

            if(emergency_snapshot.hasChild("responding_efar")){
            	var EFARs = [];
		        id_array = [];
		        try {
		            id_array = emergency_snapshot.child("responding_efar").val().split(', ');
		        }
		        catch(err) {
		            location.reload();
		        }
		        

		        for (var i = 0; i < id_array.length; i++) {
		          firebase.database().ref("/users/" + id_array[i]).on("value", function(snapshot) {
		  
		            var name = snapshot.child("name").val();
		            var phone = "No phone number avalivble"
		            if(snapshot.hasChild("phone")){
		            	phone = snapshot.child("phone").val();
		            }
		            var EFAR = {name: name, phone: phone};
		            EFARs.push(EFAR);

		          }, function (errorObject) {
		            console.log("The read failed: " + errorObject.code);
		          });
		        }
		        efars_div_title = createDiv("<strong>Responding EFAR(s):</strong>");
                efars_div_title.attribute("style", "font-size: 12px;");
                efars_div_title.parent("#info_field");

		        for (var i = EFARs.length - 1; i >= 0; i--) {
		        	efar_div = createDiv(EFARs[i].name + ": " + EFARs[i].phone);
		        	efar_div.attribute("style", "font-size: 12px; font-weight: 400;");
	                efar_div.parent("#info_field");
		        }
            }

	        //TODO: add elapsed time
	        // elapsed_time_div = createDiv("<strong>ELAPSED TIME:</strong> " + "Time Here");
	        // elapsed_time_div.attribute("style", "font-size: 10px; font-weight: 400;");
	        // elapsed_time_div.parent("#info_field");
		});
	}
}

function endEmergency(key){
	if (confirm('End this emergency?')) {
		closeInfoAndMessage();
		moveFbRecord(firebase.database().ref('/emergencies/' + key), firebase.database().ref('/completed/' + key));
		firebase.database().ref('/emergencies/' + key).remove();
	}
}

function moveFbRecord(oldRef, newRef) {    
     oldRef.once('value', function(snap)  {
          newRef.update( snap.val(), function(error) {
               if( !error ) {  oldRef.remove(); }
               else if( typeof(console) !== 'undefined' && console.error ) {  console.error(error); }
          });
     });
}

function closeInfoAndMessage(){
	info_and_messages_my_calls.hide();
	all_calls_full.show();
	messagingOpen = false;
}

var number_of_messages = 0;
var messagingOpen = false;
function draw(){
	var message_count = 0;
    firebase.database().ref("/emergencies/" + window.location.hash.substring(1) + "/messages").on('value', function(snapshot) {
        message_count = snapshot.numChildren();
    });

    if(message_count != number_of_messages){
        number_of_messages = message_count;
        if(messagingOpen){
        	updateMessages();
        }
    }
}

function updateMessages(){
	var message_field = select("#message_field");
	message_field.html("");
    firebase.database().ref("/emergencies/" + window.location.hash.substring(1) + "/messages").on('value', function(snapshot) {
        snapshot.forEach(function(snapshot) {
          var name = snapshot.child("user").val();
          var message = snapshot.child("message").val();
          current_messages = message_field.value();
          if(name == user_id){
              holderDiv = createDiv("");
              holderDiv.attribute("style", "width: 100%; display: inline-block;");
              messageDiv = createDiv(name + ":<br>" + message);
              messageDiv.attribute("class", "myMessage");
              messageDiv.parent(holderDiv);
              holderDiv.parent("#message_field");
          }else{
              holderDiv = createDiv("");
              holderDiv.attribute("style", "width: 100%; display: inline-block;");
              messageDiv = createDiv(name + ":<br>" + message);
              messageDiv.attribute("class", "otherMessage");
              messageDiv.parent(holderDiv);
              holderDiv.parent("#message_field");
          }
        });
        var textarea = document.getElementById('message_field');
        textarea.scrollTop = textarea.scrollHeight;
    });
}

function sendMessage(){
    var new_message = select("#message_to_send");
    if(new_message.value() != ""){

      var date = new Date();
      var time = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "T" +  date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
      
      var package = {
        timestamp: time,
        user: user_id, 
        message: new_message.value()
      }

      firebase.database().ref('/emergencies/' + window.location.hash.substring(1) + "/messages").push(package).then(
            function onSuccess(res) {
                new_message.value("");
          });
    }
}







