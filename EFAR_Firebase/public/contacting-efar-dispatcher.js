var key;
var number_of_messages = 0;
var last_user = "";

function setup() {
    key = window.location.hash.substring(1);
}

var efarDiv;
var found_efar = false;

function draw(){
    var state = 100;
    var completed_state; 
    var canceled_state;
    var efar_id;
    var efar_name;
    var efar_phone;
    firebase.database().ref("/emergencies/" + window.location.hash.substring(1)).on("value", function(snapshot) {
      if(snapshot.hasChild("state")){
         state = snapshot.child("state").val().toString();
      efar_id = snapshot.child("responding_efar").val();
      }
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
    });

    firebase.database().ref("/completed/" + window.location.hash.substring(1)).on("value", function(snapshot) {
      if(snapshot.hasChild("state")){
        completed_state = snapshot.child("state").val().toString();
      }
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
    });

    firebase.database().ref("/canceled/" + window.location.hash.substring(1)).on("value", function(snapshot) {
      if(snapshot.hasChild("state")){
        canceled_state = snapshot.child("state").val().toString();
      }
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
    });

    if(state == "1" || state == "1.5"){
        var text = select("#contacting_title");
        text.style("font-size", "0.5em")
        if(state == "1.5"){
          text.html("<h2>EFAR on Scene!</h2>");
          text.style("color", "#00ff00")
        }else{
          text.html("<h2>EFAR Contacted!</h2>");
          text.style("color", "#FF8C00")
        }
        
        

        var names = "N/A";
        var phones = "N/A";

        id_array = [];
        try {
            id_array = efar_id.split(', ');
        }
        catch(err) {
            location.reload();
        }
        

        for (var i = 0; i < id_array.length; i++) {
          firebase.database().ref("/users/" + id_array[i]).on("value", function(snapshot) {
            if(i === 0){
              names = snapshot.child("name").val();
              phones = snapshot.child("phone").val();
            }else{
              names = names + ", " + snapshot.child("name").val();
              phones = phones + ", " + snapshot.child("phone").val();
            }
            
          }, function (errorObject) {
            console.log("The read failed: " + errorObject.code);
          });
        }

        //update contacted card
        var contacted_card = select("#contacted_card");
        if(contacted_card != null){
          contacted_card.remove();
        }
        
        efarDiv = createDiv("<div id='contacted_card'><hr><h3><center><strong>Name(s):</strong> <i>" + names 
              + "</i><div><strong>Phone(s):</strong> <i>" + phones 
              + "</i></div><div><strong>ID(s):</strong> <i>" + efar_id 
              + "</i></div></center></h3></div>");
            efarDiv.parent("contacted_efar_info");
            var message_popup = select("#message_popup");
            message_popup.show();

        //get messages here
        var message_count = 0;
        firebase.database().ref("/emergencies/" + window.location.hash.substring(1) + "/messages").on('value', function(snapshot) {
            message_count = snapshot.numChildren();
        });

        if(message_count != number_of_messages){
            number_of_messages = message_count;
            updateMessages();
        }
    }else if(state == "0"){
        var text = select("#contacting_title");
        text.html("Contacting EFAR...");
        text.style("color", "#bb0000")
        text.style("font-size", "0.5em")
        var message_popup = select("#message_popup");
        message_popup.hide();
    }else if(canceled_state == "-2" || canceled_state == "-3"){
        var text = select("#contacting_title");
        text.html("No EFARs available near incident");
        text.style("font-size", "0.5em")
        text.style("color", "#bb0000")
        var message_popup = select("#message_popup");
        message_popup.hide();
    }else if(completed_state == "2"){
        var text = select("#contacting_title");
        text.html("Emergency Ended");
        text.style("color", "#00bb00")
        text.style("font-size", "0.5em")
        var message_popup = select("#message_popup");
        message_popup.hide();
    }else{
        var text = select("#contacting_title");
        text.html("Looking for Signal...");
        text.style("color", "#0000bb")
        text.style("font-size", "0.5em")
        var message_popup = select("#message_popup");
        message_popup.hide();
    }
}

function updateMessages(){
    var message_field = select("#message_field");
    message_field.html('');
    firebase.database().ref("/emergencies/" + window.location.hash.substring(1) + "/messages").on('value', function(snapshot) {
        snapshot.forEach(function(snapshot) {
          var user = snapshot.child("user").val();
          var message = snapshot.child("message").val();
          current_messages = message_field.value();
          if(user == user_id){
              newDiv = createDiv("<strong><u>" + user + "</u></strong>");
              newDiv.parent("message_field");
              newDiv.style("text-align", "right");
              newDiv2 = createDiv(message);
              newDiv2.parent("message_field");
              newDiv2.style("text-align", "right");
          }else{
              newDiv = createDiv("<strong><u>" + user + "</u></strong>");
              newDiv.parent("message_field");
              newDiv2 = createDiv(message);
              newDiv2.parent("message_field");
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

