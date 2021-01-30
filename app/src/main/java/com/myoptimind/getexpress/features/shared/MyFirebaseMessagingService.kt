package com.myoptimind.getexpress.features.shared

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.myoptimind.getexpress.features.customer.cart.rider_search.CustomerRiderSearchFragment
import com.myoptimind.getexpress.features.customer.cart.rider_search.data.TrackingPayload
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var appSharedPref: AppSharedPref

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Timber.d("From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty() && remoteMessage.notification != null) {
            Timber.d("Message data payload: ${remoteMessage.data}")
            val params = remoteMessage.data.toString()
            val obj = JSONObject(params).getJSONObject("data")
            val trackingPayload = TrackingPayload.createFromJsonString(obj.toString())
            remoteMessage.notification?.let {
                Timber.d("Message Notification Body: ${it.body}")
//                sendNotification(it.body.toString(), articleId)
            }
            sendBroadcast(
               CustomerRiderSearchFragment.createBroadcastIntent(trackingPayload)
            )
        }



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Timber.d("Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Timber.d("sendRegistrationTokenToServer($token)")
    }


    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val EXTRA_WHATS_NEW = "extra_whats_new"
    }
}