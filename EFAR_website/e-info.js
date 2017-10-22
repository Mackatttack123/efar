
function add_emergency() {

    var latitude = document.getElementById("latitude").value;
    var longitude = document.getElementById("longitude").value;
    
    if ((!isNaN(latitude) && latitude.toString().indexOf('.') != -1) && (!isNaN(longitude) && longitude.toString().indexOf('.') != -1)){
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
        window.location = "message.html"
    }else{
        alertify.error("Invalid cordinates.");
    }
}
