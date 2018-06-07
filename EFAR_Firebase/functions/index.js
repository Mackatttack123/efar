
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
const MAX_NUMBER_OF_EFARS_TO_NOTIFY = 25;

exports.sendNotifyAdded = functions.database.ref('/emergencies/{id}').onCreate((snap, context) => {
	return admin.database().ref('/tokens').once('value').then(function(snapshot) {
	    var efarArray = snapshotToArray(snapshot, snap.child('latitude').val(), snap.child('longitude').val());
	    efarArray.sort(function(a, b) {
		    return a.distance - b.distance;
		});
	    var payload = {
 			data: {
 				title: "NEW EMERGENCY!",
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
						return admin.database().ref("/emergencies/"+snap.key+"/state").set("-3");
					}else{
						return admin.messaging().sendToDevice(tokens_to_send_to, payload).then(response => {
							return
						});
					}
			}else{
				//no efars in range
				return admin.database().ref("/emergencies/"+snap.key+"/state").set("-2");
			}
		}else{
			//no efars avalible
			return admin.database().ref("/emergencies/"+snap.key+"/state").set("-3");
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
exports.sendNotifyMessage = functions.database.ref('/emergencies/{id}/messages/{uid}').onCreate((snap, context) => {
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

exports.sendNotifyCanceled = functions.database.ref('/canceled/{id}').onCreate((snap, context) => {
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

exports.sendNotifyCompleted = functions.database.ref('/completed/{id}').onCreate((snap, context) => {
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
          newRef.set( snap.val(), function(error) {
               if( !error ) {  oldRef.remove(); }
               else if( typeof(console) !== 'undefined' && console.error ) {  console.error(error); }
          });
     });
}

exports.cleanEmergecyData = functions.database.ref('/emergencies').onWrite((snap, context) => {
	return admin.database().ref('/emergencies').once('value', function(snapshot) {
		snapshot.forEach(function(childSnapshot) {
			if(!childSnapshot.hasChild("state")){
	    		childSnapshot.ref.remove();
	    	}else if(!childSnapshot.hasChild("other_info")){
	    		childSnapshot.ref.remove();
	    	}else if(childSnapshot.child("state").val() === "-2" || childSnapshot.child("state").val() === "-3" || childSnapshot.child("state").val() === "-4"){
	    		moveFbRecord(childSnapshot.ref, admin.database().ref('/canceled/' + childSnapshot.key));
	    	}
    	});
    	return;
	});
});

exports.cleanTokenData = functions.database.ref('/tokens/{id}').onCreate((snap, context) => {
	return admin.database().ref('/tokens').once('value', function(snapshot) {
		snapshot.forEach(function(childSnapshot) {
			// remove 0.0 corrdiate tokens
			if(childSnapshot.child("latitude").val() === 0.0 && childSnapshot.child("longitude").val() === 0.0){
				childSnapshot.ref.remove();
			}
			// remove duplicaute user tokens
			if(childSnapshot.child("token_users_name").exists() && snap.after.child("token_users_name").exists()){
				if(childSnapshot.child("token_users_name").val() === snap.after.child("token_users_name").val() && snap.after.key !== childSnapshot.key){
					console.log(childSnapshot.child("token_users_name").val());
					childSnapshot.ref.remove();
				}
			}
    	});
    	return;
	});
});



//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//POSSIBLY CAN USE THE CODE BELOW TO DELETE ALL THE DEAD ANYNOMOUS USERS FROM DATABASE
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

//Code from: https://github.com/firebase/functions-samples/tree/master/delete-unused-accounts-cron

/**
 * When requested this Function will delete every user accounts that has been inactive for 30 days.
 * The request needs to be authorized by passing a 'key' query parameter in the URL. This key must
 * match a key set as an environment variable using `firebase functions:config:set cron.key="YOUR_KEY"`.
 */

// exports.accountcleanup = functions.https.onRequest((req, res) => {
//   const key = req.query.key;

//   // Exit if the keys don't match.
//   if (!secureCompare(key, functions.config().cron.key)) {
//     console.log('The key provided in the request does not match the key set in the environment. Check that', key,
//         'matches the cron.key attribute in `firebase env:get`');
//     res.status(403).send('Security key does not match. Make sure your "key" URL query parameter matches the ' +
//         'cron.key environment variable.');
//     return null;
//   }
  
//   // Fetch all user details.
//   return getInactiveUsers().then((inactiveUsers) => {
//     // Use a pool so that we delete maximum `MAX_CONCURRENT` users in parallel.
//     const promisePool = new PromisePool(() => deleteInactiveUser(inactiveUsers), MAX_CONCURRENT);
//     return promisePool.start();
//   }).then(() => {
//     console.log('User cleanup finished');
//     res.send('User cleanup finished');
//     return null;
//   });
// });

/**
 * Deletes one inactive user from the list.
 */
// function deleteInactiveUser(inactiveUsers) {
//   if (inactiveUsers.length > 0) {
//     const userToDelete = inactiveUsers.pop();

//     // Delete the inactive user.
//     return admin.auth().deleteUser(userToDelete.uid).then(() => {
//       console.log('Deleted user account', userToDelete.uid, 'because of inactivity');
//       return null;
//     }).catch(error => {
//       console.error('Deletion of inactive user account', userToDelete.uid, 'failed:', error);
//       return null;
//     });
//   }
//   return null;
// }

/**
 * Returns the list of all inactive users.
 */
// function getInactiveUsers(users = [], nextPageToken) {
//   return admin.auth().listUsers(1000, nextPageToken).then((result) => {   
//     // Find users that have not signed in in the last 30 days.
//     const inactiveUsers = result.users.filter(
//         user => Date.parse(user.metadata.lastSignInTime) < (Date.now() - 30 * 24 * 60 * 60 * 1000));
    
//     // Concat with list of previously found inactive users if there was more than 1000 users.
//     users = users.concat(inactiveUsers);
    
//     // If there are more users to fetch we fetch them.
//     if (result.pageToken) {
//       return getInactiveUsers(users, result.pageToken);
//     }
    
//     return users;
//   });
// }