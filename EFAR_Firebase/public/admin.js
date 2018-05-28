function setup() {
	var efar_data_table = select("#efar_data_table");
    firebase.database().ref("/users/").on('value', function(snapshot) {
        snapshot.forEach(function(snapshot) {
        	var id = snapshot.child("id").val();
        	var name = snapshot.child("name").val();
        	var lat = snapshot.child("latitude").val();
        	var long = snapshot.child("longitude").val();
        	var phone = snapshot.child("phone").val();
        	var logged_in = snapshot.child("logged_in").val();

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
    var address = document.getElementById("new_EFAR_Address").value;

    if(id != "" && name != "" && phone != ""){

        var EFAR_package = {
            id: id, 
            name: name,
            phone: phone,
            address: address
        }

        //TODO: Maybe check if id is already in the database so someone doesnt get overwritten?

        firebase.database().ref("/users/" + id).set(EFAR_package).then(
                function onSuccess(res) {
                    document.getElementById("new_EFAR_ID").value = "";
                    document.getElementById("new_EFAR_Name").value = "";
                    document.getElementById("new_EFAR_Phone").value = "";
                    document.getElementById("new_EFAR_Address").value = "";
                    location.reload();
              });
    }else{
        alert("You cannot leave any field empty.");
    }
        
}