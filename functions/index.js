function Efar (name, latitude, longitude, token) {

    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.distance = 0.0;
    this.token = token;

    // setter 
    this.setDistance = function(dist) { this.distance = dist; }
}

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

efarArray = [];

exports.sendPushNotification = functions.database.ref('/emergencies/{id}').onCreate(event => {
	const payload = {
		notification: {
			title: "NEW EMERGANCY:",
			body: "Info given: \"" + event.data.child('other_info').val() + "\"",
			badge: '1',
			sound: 'default',
		}
	};

	efarArray = [];
	return admin.database().ref('/users').on('value', function(snapshot){
	    snapshot.forEach(function(child){
	        var name = child.child("name").val();
	        var lat = 0.0;
	        var long = 0.0;
	        var token = "token"
	        if(child.child("latitude").exists()){
	        	lat = child.child("latitude").val();
	        }
	        if(child.child("longitude").exists()){
		        long = child.child("longitude").val();
		    }
		    if(child.child("token").exists()){
		        token = child.child("token").val();
		    }
	        efarArray.push(new Efar(name, lat, long, token));
	    });


	    sortEfars(event.data.child('latitude').val(), event.data.child('longitude').val());


		for (var i = 0; i < efarArray.length; i++) {
				var new_key = admin.database().ref('/sorted/' + efarArray[i].name);
		        new_key.child("distance").set(efarArray[i].distance);
		}

		return admin.database().ref('tokens').once("value").then(allToken => {
			if (allToken.val()){
				const token = Object.keys(allToken.val());
				return admin.messaging().sendToDevice(token, payload).then(response => {

				});
			};
		});
	});

		
});

exports.sendPushNotificationDeleted = functions.database.ref('/emergencies/{id}').onDelete(event => {
	const payload = {
		notification: {
			title: "Emergency Over:",
			body: 'An emergancy in your area has been cancled or is over now...',
			badge: '1',
			sound: 'default',
		}
	};
	return admin.database().ref('tokens').once("value").then(allToken => {
		if (allToken.val()){
			const token = Object.keys(allToken.val());
			return admin.messaging().sendToDevice(token, payload).then(response => {

			});
		};
	});
});

function distance(lat1, lon1, lat2, lon2) {
	var radlat1 = Math.PI * lat1/180;
	var radlat2 = Math.PI * lat2/180;
	var theta = lon1-lon2;
	var radtheta = Math.PI * theta/180;
	var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
	dist = Math.acos(dist);
	dist = dist * 180/Math.PI;
	dist = dist * 60 * 1.1515;
	dist = dist * 1.609344;
	return dist;
}

// TODO: For some odd reason the distance setter or caulator is returning null? or not working
/* POSSIBLE WAY TO KEEP TRACK OF SORTED LISTS?
	sorted
	|
	|
	 -> KEY : JGYUKAHISLJDHJV
	 	|
	 	|
	 	 -> mack
	 	|
	 	|
	 	 -> jim
	 	|
	 	|
	 	 -> etc....
	|
	|
	 -> KEY : GUILKJFGKHLJHLJ
	|
	|
	 -> KEY : LJKHGFJKL;JHJLKH
*/

sortEfars = function(e_lat, e_long) {
	/*for (var i = 0; i < efarArray.length; i++) {
	        efarArray[i].setDistance(distance(e_lat, e_long, efarArray[i].latitude, efarArray[i].longitute));
	}*/

	efarArray.sort(function(a, b) { 
	    return a.distance - b.distance;
	});
}
	



