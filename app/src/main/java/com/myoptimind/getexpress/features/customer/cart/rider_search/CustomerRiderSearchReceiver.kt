package com.myoptimind.getexpress.features.customer.cart.rider_search

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import timber.log.Timber



class CustomerRiderSearchReceiver: BroadcastReceiver() {






    override fun onReceive(context: Context?, intent: Intent?) {
        StringBuilder().apply {
            append("Action: ${intent?.action}\n")
            append("URI: ${intent?.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                Timber.d(log)
                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
            }
        }
    }



}