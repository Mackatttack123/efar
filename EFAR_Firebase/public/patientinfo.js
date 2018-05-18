var key = "key";
var progressDiv;
var checking_for_send = false;
function add_emergency() {

    var phone_number = document.getElementById("phoneNumber").value;
    var other_info = document.getElementById("information").value;
    var date = new Date();
    var time = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "T" +  date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    if (!navigator.geolocation) {

      var call_efar_button = document.getElementById("call_efar_button").value;
      call_efar_button.disabled = true;
      alert("Your browser does not support geolocation and we will not be able to contact EFAR.");

    } else {
        
        var lat;
        var lng;

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
        checking_for_send = true;

        navigator.geolocation.getCurrentPosition(function(position) {

            // Get the coordinates of the current possition.
            lat = position.coords.latitude;
            lng = position.coords.longitude;  
            var package = {
                phone_number: phone_number, 
                other_info: other_info,
                latitude: lat,
                longitude: lng,
                creation_date: time,
                state: "0"
            }
            progressDiv.attribute("aria-valuenow", "100");
            key = firebase.database().ref('/emergencies/').push(package).getKey();
        });
    }
}

function draw(){
    if(checking_for_send){
        firebase.database().ref("/emergencies/" + key).on("value", function(snapshot) {
          state = snapshot.child("state").val();
        }, function (errorObject) {
          console.log("The read failed: " + errorObject.code);
        });
        try{
            if(state){
                window.location = "contacting-efar.html#" + key;
            }
        }catch(err) {

        }
    }
}

