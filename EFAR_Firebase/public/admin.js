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

        	newDiv = createElement("tr", "<td>" + id 
        		+ "</td><td>" + name 
        		+ "</td><td>" + phone 
        		+ "</td><td>" + lat 
        		+ "</td><td>" + long 
        		+ "</td><td>" + logged_in + "</td>");
        	newDiv.parent("efar_data_table");
        });
    });
}