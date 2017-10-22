function didEnterVal(input) {
    if (input != null && input != "") {
        return true;
    }
    else {
        return false;
    }
}

function check_info() {
    var lat_entered = document.getElementById("latitude").value;
    var long_entered = document.getElementById("longitude").value;
    var address_entered = document.getElementById("address").value;

    if (didEnterVal(lat_entered) && didEnterVal(long_entered)) {
        window.location = "message.html"
    }
    if (didEnterVal(address_entered)) {
        window.location = "message.html"
    }
}

function add_emergency() {

    var latitude = document.getElementById("latitude").value;
    var longitude = document.getElementById("longitude").value;
    var other_info = document.getElementById("notes").value;

    var date = new Date();
    var time = (date.getYear() + 1900) + "-" + (date.getMonth() + 1) + "-" + date.getDate() + "T" +  date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    
    var package = {
        phone_number: "", 
        other_info: other_info,
        latitude: latitude,
        longitude: longitude,
        creation_date: time,
        state: "0"}
    firebase.database().ref('/emergencies/').push(package);
}
