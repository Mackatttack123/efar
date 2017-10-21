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