package com.myoptimind.getexpress.features.rider.rider_history.data

import com.google.gson.annotations.SerializedName

data class RiderHistory(
    val location: HistoryLocation,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("total_items")
    val totalItems: String? = null,
    @SerializedName("total_price")
    val totalPrice: String? = null,
    val icon: String? = null
){
    inner class HistoryLocation(
        var label: String? = null,
        @SerializedName("address_text")
        var addressText: String? = null,
        var latitude: String? = null,
        var longitude: String? = null,
        var landmark: String? = null
    )
}
