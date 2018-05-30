var key = "key";
var progressDiv;
var checking_for_send = false;
var location_ready = false;
var lat;
var long; 
var keys;

function add_emergency() {

    //set up progress bar
    progressDiv = createDiv("Preparing to contact EFAR...");
    progressDiv.addClass("progress-bar progress-bar-striped progress-bar-animated");
    progressDiv.attribute("role", "progressbar");
    progressDiv.attribute("aria-valuenow", "75");
    progressDiv.attribute("aria-valuenmin", "0");
    progressDiv.attribute("aria-valuemax", "100");
    progressDiv.style("width", "75%");
    progressDiv.parent("loading_bar_container");
    select("#call_efar_button").hide();
    select("#cancel_button").hide();

    var address = document.getElementById("address").value;
    //get the lat and long for address here via google
    var geocoder = new google.maps.Geocoder();
    geocoder.geocode( { 'address': address}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        lat = results[0].geometry.location.lat();
        long = results[0].geometry.location.lng();
        location_ready = true;
      }else{
        alert("Invalid address data!");
        location.reload();
      }
    }); 
}

function setup() {
    updateCurrentCalls();
}

function draw(){

    if(frameCount % 360 === 0){
        updateCurrentCalls();
    }

    if(location_ready){
        var other_info = document.getElementById("information").value;
        var reference_number = document.getElementById("reference_number").value;
        var call_received_time = document.getElementById("call_received_time").value;
        var date = new Date();
        var time = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "T" +  date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();

        var package = {
            emergency_made_by_dispatcher: user_id,
            phone_number: "N/A",
            reference_number: reference_number,
            call_received_time: call_received_time,
            other_info: other_info,
            latitude: lat,
            longitude: long,
            creation_date: time,
            state: "0"
        }

        location_ready = false;
        progressDiv.attribute("aria-valuenow", "100");
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
        checking_for_send = true;
    }
    if(checking_for_send){
        firebase.database().ref('/emergencies/' + key + '/state').set(0).then(
            function onSuccess(res) {

                window.location = "contacting-efar-dispatcher.html#" + key;

            });
    }
}

function updateCurrentCalls(){

    var myNode = document.getElementById("Current_calls_field");
    myNode.innerHTML = '';

    firebase.auth().onAuthStateChanged(function(user) {
        if (user) {
            // User is signed in.
            var user_email = user.email;
            var user_name = user_email.replace('@email.com','');

            firebase.database().ref("/dispatchers/" + user_name + "/current_calls").on('value', function(snapshot) {

                var myNode = document.getElementById("Current_calls_field");
                myNode.innerHTML = '';

                snapshot.forEach(function(snapshot) {
                  var key = snapshot.child("key").val();
                  var main_key = snapshot.key;

                    firebase.database().ref("/emergencies/").on('value', function(emergencies_snapshot) {
                        if(emergencies_snapshot.hasChild(key)){
                            var creation_date = emergencies_snapshot.child(key).child("creation_date").val();
                            var other_info = emergencies_snapshot.child(key).child("other_info").val();
                            var state = emergencies_snapshot.child(key).child("state").val();

                            newDiv = createDiv("<strong>Created at: </strong>" + creation_date.replace("T", " ~ "));
                            newDiv.parent("Current_calls_field");

                            newDiv2 = createDiv("<strong>Info Given: </strong>" + other_info);
                            newDiv2.parent("Current_calls_field");

                            if(state == "0"){
                                newDiv3 = createDiv("Contacting EFAR...");
                                newDiv3.style("color", "#bb0000");
                                newDiv3.parent("Current_calls_field");
                            }else if(state == "1"){
                                newDiv3 = createDiv("EFAR Contacted!");
                                newDiv3.style("color", "#FF8C00");
                                newDiv3.parent("Current_calls_field");
                            }else if(state == "1.5"){
                                newDiv3 = createDiv("EFAR on Scene!");
                                newDiv3.style("color", "#00bb00");
                                newDiv3.parent("Current_calls_field");
                            }
                            

                            if(emergencies_snapshot.child(key).hasChild("responding_efar")){
                                var responding_efar = emergencies_snapshot.child(key).child("responding_efar").val().toString();
                                /* 
                                var efar_id = responding_efar;
                                var names = "N/A";
                                var phones = "N/A";

                                var id_array = efar_id.split(', ');
                                console.log(id_array);

                                for (var i = 0; i < id_array.length; i++) {
                                  firebase.database().ref("/users/" + id_array[i]).on("value", function(snapshot) {
                                    if(i === 0){
                                      names = snapshot.child("name").val();
                                      phones = snapshot.child("phone").val();
                                      console.log(names);
                                    }else{
                                      names = names + ", " + snapshot.child("name").val();
                                      phones = phones + ", " + snapshot.child("phone").val();
                                      console.log(names);
                                    }
                                    
                                  }, function (errorObject) {
                                    console.log("The read failed: " + errorObject.code);
                                  });
                                }
        
                                newDiv3 = createDiv("<strong>Responding EFAR: </strong>" + names);
                                newDiv3.parent("Current_calls_field");
                                */

                                newDiv3 = createDiv("<strong>Responding EFAR(s): </strong>" + responding_efar);
                                newDiv3.parent("Current_calls_field");
                            }

                            newDiv5 = createDiv("<a href='contacting-efar-dispatcher.html#" + key + "'>Click Here For More & EFAR Chat</a>");
                            newDiv5.parent("Current_calls_field");

                            newDiv4 = createDiv("<p></p>");
                            newDiv4.parent("Current_calls_field");

                            var textarea = document.getElementById('Current_calls_field');
                            textarea.scrollTop = textarea.scrollHeight;
                        }else{
                            firebase.database().ref("/dispatchers/" + user_name + "/current_calls/" + main_key).remove();
                        }
                    });
                });
            });
        } 
    });        
}


