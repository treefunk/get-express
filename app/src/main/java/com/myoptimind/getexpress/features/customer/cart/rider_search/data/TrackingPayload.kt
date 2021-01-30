package com.myoptimind.getexpress.features.customer.cart.rider_search.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

@Parcelize
data class TrackingPayload(
        val forcePayment: String,
        val type: String,
        val status: String
): Parcelable {
    companion object {
        fun createFromJsonString(jsonString: String): TrackingPayload {
            val jsonObject = JSONObject(jsonString)
            return TrackingPayload(
                    jsonObject.optInt("force_payment").toString(),
                    jsonObject.optString("change_status"),
                    jsonObject.optString("status")
            )
        }
    }
}