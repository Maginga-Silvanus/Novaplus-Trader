package com.example.novaplustrader.util

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NovaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store or synchronize FCM registration token with secure backend
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "NovaPlus Trader Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "A new notification is available."

        val notificationManager = NovaNotificationManager(applicationContext)
        notificationManager.notifyBotEvent(title = title, message = body)
    }
}
