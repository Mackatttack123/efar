var key;
var number_of_messages = 0;
var last_user = "";

function setup() {
    key = window.location.hash.substring(1);
}

var efarDiv;
var found_efar = false;

function draw(){
    var state; 
    var efar_id;
    var efar_name;
    var efar_phone;
    firebase.database().ref("/emergencies/" + window.location.hash.substring(1)).on("value", function(snapshot) {
      state = snapshot.child("state").val();
      efar_id = snapshot.child("responding_efar").val();
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
    });
    if(state == "1"){
        var text = select("#contacting_title");
        text.html("<h2>EFAR Contacted!</h2>");
        text.style("color", "#00bb00")

        firebase.database().ref("/users/" + efar_id).on("value", function(snapshot) {
          efar_name = snapshot.child("name").val();
          efar_phone = snapshot.child("phone").val();
          if(!found_efar){
            found_efar = true;
              efarDiv = createDiv("<hr><h3><center><strong>Name:</strong> <i>" + efar_name 
                + "</i><div><strong>Phone:</strong> <i>" + efar_phone 
                + "</i></div><div><strong>ID:</strong> <i>" + efar_id 
                + "</i></div></center></h3>");
              efarDiv.parent("contacted_efar_info");
              var message_popup = select("#message_popup");
              message_popup.show();
          } 
        }, function (errorObject) {
          console.log("The read failed: " + errorObject.code);
        });

        var message_count = 0;
        firebase.database().ref("/emergencies/" + window.location.hash.substring(1) + "/messages").on('value', function(snapshot) {
            message_count = snapshot.numChildren();
        });

        if(message_count != number_of_messages){
            var badge = select('#badge');
            badge.html(message_count);
            number_of_messages = message_count;
            updateMessages();
        }

    }else if(state == "0"){
        var text = select("#contacting_title");
        text.html("Contacting EFAR...");
        text.style("color", "#bb0000")
        var message_popup = select("#message_popup");
        message_popup.hide();
    }else{
        var text = select("#contacting_title");
        text.html("Signal lost.");
        text.style("color", "#0000bb")
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
    var package = {
        user: user_id, 
        message: new_message.value()
    }
    firebase.database().ref('/emergencies/' + window.location.hash.substring(1) + "/messages").push(package).then(
            function onSuccess(res) {
                new_message.value("");
          });
}

