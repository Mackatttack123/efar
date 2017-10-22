var vm = this;

vm.e-info = e-info;

function didEnterVal(input) {
    var valid = false;
    if (input != null && input != "") {
        valid = true;
    }
    return valid;
}

function check_info() {
    var lat_entered = document.getElementById("latitude").value;
    var long_entered = document.getElementById("longitude").value;
    var address_entered = document.getElementById("address").value;

    if ((didEnterVal(lat_entered) && didEnterVal(long_entered)) || didEnterVal(address_entered)) {
        window.location = "message.html"
    }
}