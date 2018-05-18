var key = "key";
var progressDiv;
var checking_for_send = false;
var location_ready = false;
var lat;
var long; 

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

    lat = document.getElementById("lat").value;
    long = document.getElementById("long").value;
    var address = document.getElementById("address").value;
    if(lat.length != 0 && long.length != 0 && !isNaN(lat) && !isNaN(long)){
        location_ready = true;
    }else if(address.length != 0){
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
    }else{
        alert("Invalid location data!");
        location.reload();
    } 
}

function draw(){
    if(location_ready){
        var other_info = document.getElementById("information").value;
        var date = new Date();
        var time = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "T" +  date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();

        var package = {
            phone_number: "N/A", 
            other_info: other_info,
            latitude: lat,
            longitude: long,
            creation_date: time,
            state: "0"
        }
        location_ready = false;
        progressDiv.attribute("aria-valuenow", "100");
        key = firebase.database().ref('/emergencies/').push(package).getKey();
        checking_for_send = true;
    }
    if(checking_for_send){
        firebase.database().ref('/emergencies/' + key + '/state').set(0).then(
            function onSuccess(res) {
                window.location = "contacting-efar-dispatcher.html#" + key;
          });
    }
}

