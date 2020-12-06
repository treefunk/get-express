package com.myoptimind.getexpress.features.rider.selected_customer_request.data

import com.google.gson.annotations.SerializedName

data class CartLocation(
        val label: String = "",
        @SerializedName("address_text")
        val addressText: String = "",
        @SerializedName("latitude")
        val latitude: String = "",
        @SerializedName("longitude")
        val longitude: String = "",
        val landmark: String = ""
)