
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

const MAX_EFAR_TRAVEL_RADIUS = 2.0; //in kilometers
const MAX_NUMBER_OF_EFARS_TO_NOTIFY = 5;

exports.sendPushAdded = functions.database.ref('/emergencies/{id}').onCreate((snap, context) => {
	return admin.database().ref('/tokens').once('value').then(function(snapshot) {
	    var efarArray = snapshotToArray(snapshot, snap.child('latitude').val(), snap.child('longitude').val());
	    efarArray.sort(function(a, b) {
		    return a.distance - b.distance;
		});
	    var payload = {
 			data: {
 				title: "NEW EMERGANCY!",
				body: "Patient Message: " + snap.child('other_info').val(),
 				//badge: '1',
 				sound: 'default',
 			}
 		};

		//check if any efars are online
		if(efarArray.length > 0){
			if(efarArray[0].distance < MAX_EFAR_TRAVEL_RADIUS){
				tokens_to_send_to = [];
					if(efarArray.length >= MAX_NUMBER_OF_EFARS_TO_NOTIFY){
						//only send to the 5 closest efars
						for (var i = MAX_NUMBER_OF_EFARS_TO_NOTIFY-1; i >= 0; i--) {
							//only send to efars in range
							if (efarArray[i].distance < MAX_EFAR_TRAVEL_RADIUS) {
								tokens_to_send_to.push(efarArray[i].token);
								console.log(efarArray[i].token);
							}
						}
					}else{
						for (var j = efarArray.length - 1; j >= 0; j--) {
							//only send to efars in range
							if (efarArray[j].distance < MAX_EFAR_TRAVEL_RADIUS) {
								tokens_to_send_to.push(efarArray[j].token);
								console.log(efarArray[j].token);
							}
						}
					}
					// check if an efar created the emergency and if so don't send them a notification
					for (var k = tokens_to_send_to.length - 1; k >= 0; k--) {
						if(tokens_to_send_to[k] === snap.child('emergency_made_by_efar_token').val()){
							tokens_to_send_to.splice(k, 1);
						}
					}
					console.log(tokens_to_send_to.length);
					if(tokens_to_send_to.length < 1){
						//no efars avalible
						return admin.database().ref("/emergencies/"+snap.key+"/state").set(-3);
					}else{
						return admin.messaging().sendToDevice(tokens_to_send_to, payload).then(response => {
							return
						});
					}
			}else{
				//no efars in range
				return admin.database().ref("/emergencies/"+snap.key+"/state").set(-2);
			}
		}else{
			//no efars avalible
			return admin.database().ref("/emergencies/"+snap.key+"/state").set(-3);
		}
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
}

//send notifications to efars when they get messages 
exports.sendPushMessage = functions.database.ref('/emergencies/{id}/messages/{uid}').onCreate((snap, context) => {
	var user = snap.child("user").val()
	const payload = {
		data: {
			title: "Message (" + user + "):",
			body: snap.child("message").val(),
			//badge: '1',
			sound: 'default',
		}
	};
	//send notifications to all responding efars
	return admin.database().ref('/emergencies/' + snap.ref.parent.parent.getKey()).once('value').then(parentSnap => {
		var responding_efars = parentSnap.child("responding_efar").val();
		var id_array = responding_efars.split(", ");
		return admin.database().ref("/users/").once('value').then((snapshot) => {
			var tokens_to_send_to = [];
			for (var i = id_array.length - 1; i >= 0; i--) {
				var token = snapshot.child(id_array[i]).child("token").val();
				//make sure we don't send notifications to the efar who is sending the message
				if(!(user === snapshot.child(id_array[i]).child("name").val())){
				   tokens_to_send_to.push(token);
				}
				console.log(id_array[i]);
				console.log(tokens_to_send_to[i]);
			} 
			return admin.messaging().sendToDevice(tokens_to_send_to, payload).then(response => {
				return
			});
		});
    });	
});

exports.sendPushCanceled = functions.database.ref('/canceled/{id}').onCreate((snap, context) => {
	const payload = {
		data: {
			title: "Emergency Canceled:",
			body: 'Your responding emergancy was canceled.',
			//badge: '1',
			sound: 'default',
		}
	};
	//send notifications to all responding efars
	if(snap.hasChild("responding_efar")){
		var responding_efars = snap.child("responding_efar").val();
		var id_array = responding_efars.split(", ");
		return admin.database().ref("/users/").once('value').then((snapshot) => {
			var tokens_to_send_to = [];
			for (var i = id_array.length - 1; i >= 0; i--) {
				var token = snapshot.child(id_array[i]).child("token").val();
				tokens_to_send_to.push(token);
				console.log(id_array[i]);
			} 
			return admin.messaging().sendToDevice(tokens_to_send_to, payload).then(response => {
				return
			});
		});
	}else{
		return;
	}
});

exports.sendPushCompleted = functions.database.ref('/completed/{id}').onCreate((snap, context) => {
	const payload = {
		data: {
			title: "Emergency Over:",
			body: 'Your responding emergancy was ended.',
			//badge: '1',
			sound: 'default',
		}
	};
	//send notifications to all responding efars
	if(snap.hasChild("responding_efar")){
		var responding_efars = snap.child("responding_efar").val();
		var id_array = responding_efars.split(", ");
		return admin.database().ref("/users/").once('value').then((snapshot) => {
			var tokens_to_send_to = [];
			for (var i = id_array.length - 1; i >= 0; i--) {
				var token = snapshot.child(id_array[i]).child("token").val();
				tokens_to_send_to.push(token);
				console.log(id_array[i]);
			} 
			return admin.messaging().sendToDevice(tokens_to_send_to, payload).then(response => {
				return
			});
		});
	}else{
		return;
	}
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

function moveFbRecord(oldRef, newRef) {    
     oldRef.once('value', function(snap)  {
          newRef.push( snap.val(), function(error) {
               if( !error ) {  oldRef.remove(); }
               else if( typeof(console) !== 'undefined' && console.error ) {  console.error(error); }
          });
     });
}

exports.checkStates = functions.database.ref('/emergencies').onWrite((snap, context) => {
	return admin.database().ref('/emergencies').once('value', function(snapshot) {
		snapshot.forEach(function(childSnapshot) {
			console.log(childSnapshot.child("state").val().toString());
			var state = childSnapshot.child("state").val().toString().trim();
	    	if(state === "-2" || state === "-3" || state === "-4"){
	    		moveFbRecord(childSnapshot.ref, admin.database().ref('/canceled'));
	    	}
    	});
    	return;
	});
});

exports.checkNewTokens = functions.database.ref('/tokens/{token_id}').onCreate((snap, context) => {
	if(snap.child("latitude").val() === 0.0 && snap.child("longitude").val() === 0.0){
		snap.ref.remove();
	}
	return;
});