function Efar (distance_away, token, message) {
    this.distance_away = distance_away;
    this.token = token;
    this.message = message;

    this.getToken = function() {
        return this.token;
    }
    this.getMessage = function() {
        return this.message;
    }
}

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendPushNotificationAdded = functions.database.ref('/emergencies/{id}').onCreate(event => {
	return admin.database().ref('/tokens').on('value', function(snapshot) {
	    var efarArray = snapshotToArray(snapshot, event.data.child('latitude').val(), event.data.child('longitude').val());
	    efarArray.sort(function(a, b) {
		    return a.distance - b.distance;
		});
	    var payload = {
			notification: {
				title: "NEW EMERGANCY!",
				body: "Info given: " + event.data.child('other_info').val(),
				//badge: '1',
				sound: 'default',
			}
		};
		tokens_to_send_to = [];
		if(efarArray.length >= 5){
			//only send to the 5 closest efars
			for (var i = 4; i >= 0; i--) {
				tokens_to_send_to.push(efarArray[i].token);
			}
		}else{
			for (var i = efarArray.length - 1; i >= 0; i--) {
				tokens_to_send_to.push(efarArray[i].token);
			}
		}
		//TODO: send a messaged back to patient if no efars respond or are found?
	    return admin.messaging().sendToDevice(tokens_to_send_to, payload).then(response => {
					
		});
	});
});

//code for function below from https://ilikekillnerds.com/2017/05/convert-firebase-database-snapshotcollection-array-javascript/
function snapshotToArray(snapshot, incoming_latitude, incoming_longitude) {
    var returnArr = [];

    snapshot.forEach(function(childSnapshot) {
    	var distance_to_efar = distance(childSnapshot.child('latitude').val(), childSnapshot.child('longitude').val(), incoming_latitude, incoming_longitude);
        var item = {
        	latitude: childSnapshot.child('latitude').val(), 
        	longitude: childSnapshot.child('longitude').val(),
        	token: childSnapshot.key,
        	distance: distance_to_efar
        };

        returnArr.push(item);
    });

    return returnArr;
};

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