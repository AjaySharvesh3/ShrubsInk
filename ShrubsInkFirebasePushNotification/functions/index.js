'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.send_like_notification = functions.firestore.document("user_bio/{user_id}/notifications/{notification_id}").onWrite(event => {

    const user_id = event.params.user_id;
    const notification_id = event.params.notification_id;

    console.log("User Id: " + user_id + " | Notification ID: " + notification_id);
    

})