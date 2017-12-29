function Efar (distance_away, token, message) {
    this.distance_away = distance_away;
    this.token = token;
    this.message = message;

    function getToken() {
        return token;
    };
    function getMessage() {
        return message;
    };
}

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

efarArray = [];

exports.sendPushNotificationAdded = functions.database.ref('/emergencies/{id}').onCreate(event => {
	return admin.database().ref('tokens').once("value").then(allToken => {
		if (allToken.val()){
			const token = Object.keys(allToken.val());
			tokenSplit = String(token).split(",");
			buildEFARs(tokenSplit, event.data.child('latitude').val(), event.data.child('longitude').val());
			sortEfars();
			/*realtoken = tokenSplit[0];
			admin.database().ref('tokens/' + realtoken).once('value').then(function(snapshot) {
				var distance_to_efar = distance(parseFloat(snapshot.child('latitude').val()), parseFloat(snapshot.child('longitude').val()), event.data.child('latitude').val(), event.data.child('longitude').val());
				efarArray.push(new Efar(distance_to_efar, realtoken));
				var payload = {
					notification: {
						title: "NEW EMERGANCY!",
						//body: "Info given: \"" + event.data.child('other_info').val() + "\"",
						body: "Distace from you: " + distance_to_efar + "km numEFAR: " + efarArray.length,
						//badge: '1',
						sound: 'default',
					}
				};
				return admin.messaging().sendToDevice(token, payload).then(response => {
					
				});
			});*/
			return admin.messaging().sendToDevice(efarArray[0].getToken, efarArray[0].getMessage).then(response => {
					
			});
		};
	});
});

function buildEFARs(tokenlist, emergancy_lat, emergancy_long){
	for (var i = tokenlist.length - 1; i >= 0; i--) {
		admin.database().ref('tokens/' + tokenlist[i]).once('value').then(function(snapshot) {
			var distance_to_efar = distance(parseFloat(snapshot.child('latitude').val()), parseFloat(snapshot.child('longitude').val()), emergancy_lat, emergancy_long);
			var payload = {
				notification: {
					title: "NEW EMERGANCY!",
					//body: "Info given: \"" + event.data.child('other_info').val() + "\"",
					body: "Distace from you: " + distance_to_efar + "km",
					//badge: '1',
					sound: 'default',
				}
			};
			efarArray.push(new Efar(distance_to_efar, tokenlist[i], payload));
		});
	}
}

exports.sendPushNotificationCanceled = functions.database.ref('/canceled/{id}').onCreate(event => {
	const payload = {
		notification: {
			title: "Emergency Canceled:",
			body: 'An emergancy in your area has been canceled.',
			//badge: '1',
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

exports.sendPushNotificationCompleted = functions.database.ref('/completed/{id}').onCreate(event => {
	const payload = {
		notification: {
			title: "Emergency Over:",
			body: 'An emergancy in your area is now over.',
			//badge: '1',
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
	var radlat1 = Math.PI * lat1/180.0;
	var radlat2 = Math.PI * lat2/180.0;
	var theta = lon1-lon2;
	var radtheta = Math.PI * theta/180.0;
	var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
	dist = Math.acos(dist);
	dist = dist * 180.0/Math.PI;
	dist = dist * 60.0 * 1.1515;
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

function sortEfars() {
	efarArray.sort(function(a, b) { 
	    return a.distance_away - b.distance_away;
	});
}

/*exports.sendPushNotification = functions.database.ref('/emergencies/{id}').onCreate(event => {
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
	return admin.database().ref('tokens').once("value").then(allToken => {
		if (allToken.val()){
			const token = Object.keys(allToken.val());
			return admin.messaging().sendToDevice(token, payload).then(response => {

			});
		};
	});
		
});*/
	



