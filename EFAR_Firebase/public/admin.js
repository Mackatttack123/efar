function setup() {
    canvas = createCanvas(0, 0);
    canvas.remove();
    checkGrapple();
    populateEFARTable();
}

function checkGrapple(){
    firebase.database().ref("/dispatchers/" + user_id).once('value', function(snapshot) {
        if(snapshot.child("grapple").exists()){
            if(!(snapshot.child("grapple").val().trim() === "hook")){
                window.location = 'index.html';
            }
        }
    });
}

function populateEFARTable(){
    firebase.database().ref("/users").on('value', function(snapshot) {
        var efar_data_table_body = select("#efar_data_table_body");
        efar_data_table_body.html("");
        snapshot.forEach(function(childSnapshot) {
            checkGrapple();
            var id = childSnapshot.child("id").val();
            var name = childSnapshot.child("name").val();
            var lat = childSnapshot.child("latitude").val();
            var long = childSnapshot.child("longitude").val();
            var phone;
            if(childSnapshot.hasChild("phone")){
                phone = childSnapshot.child("phone").val();
            }else{
                phone = "N/A";
            }
            
            var logged_in = childSnapshot.child("logged_in").val();

            if(lat === null){
                lat = "N/A";
            }
            if(long === null){
                long = "N/A";
            }
            if(logged_in === null){
                logged_in = "N/A";
            }

            newDiv = createElement("tr", "<td>" + id 
                + "</td><td>" + name 
                + "</td><td>" + phone 
                + "</td><td>" + lat 
                + "</td><td>" + long 
                + "</td><td>" + logged_in + "</td>");
            newDiv.parent("efar_data_table_body");
        });
    });
}

function addNewEFAR(){
    var id = document.getElementById("new_EFAR_ID").value;
    var name = document.getElementById("new_EFAR_Name").value;
    var phone = document.getElementById("new_EFAR_Phone").value;

    if(id != "" && name != ""){

        if(phone === null || phone == ""){
            phone = "";
        }

        var EFAR_package = {
            id: id, 
            name: name,
            phone: phone
        }

        //TODO: Maybe check if id is already in the database so someone doesnt get overwritten?
        firebase.database().ref("/users/" + id).set(EFAR_package).then(
                function onSuccess(res) {
                    document.getElementById("new_EFAR_ID").value = "";
                    document.getElementById("new_EFAR_Name").value = "";
                    document.getElementById("new_EFAR_Phone").value = "";
                    location.reload();
              });
    }else{
        alert("You cannot leave the Name or ID field empty!");
    }
        
}