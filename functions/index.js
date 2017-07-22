const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendPushNotification = functions.database.ref('/emergencies/{id}').onCreate(event => {
	const payload = {
		notification: {
			title: "NEW EMERGANCY:",
			body: "Info given: \"" + event.data.child('other_info').val() + "\"",
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
