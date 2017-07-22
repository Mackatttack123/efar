const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendPushNotification = functions.database.ref('/emergencies').onWrite(event => {
	const payload = {
		notification: {
			title: 'New message',
			body: 'Hello World',
			badge: '1',
			sound: 'default',
		}
	};
	return admin.database.ref('fcmToken').once("Value").then(allToken => {
		if (allToken.val()){
			const token = Object.keys(allToken.val());
			return admin.messaging().sendToDivice(token, payload).then(responce => {

			});
		};
	});
)};
