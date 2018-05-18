function setup() {
    key = window.location.hash.substring(1);
}

function draw(){
    var state;
    firebase.database().ref("/emergencies/" + window.location.hash.substring(1)).on("value", function(snapshot) {
      state = snapshot.child("state").val();
    }, function (errorObject) {
      console.log("The read failed: " + errorObject.code);
    });
    if(state == "1"){
        var text = select("#cantacting_title");
        text.html("EFAR Contacted!");
        text.style("color", "#00bb00")
    }else if(state == "0"){
        var text = select("#cantacting_title");
        text.html("Contacting EFAR...");
        text.style("color", "#bb0000")
    }else{
        var text = select("#cantacting_title");
        text.html("Signal lost.");
        text.style("color", "#0000bb")
    }
}