package com.myoptimind.get_express.features.rider.rider_history.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.rider.customer_requests_list.data.CustomerRequest

data class RiderHistory(
        val id: String,
        @SerializedName("cart_id")
        val cartId: String,
        @SerializedName("service_id")
        val serviceId: String,
        val location: HistoryLocation,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("total_items")
        val totalItems: String? = null,
        @SerializedName("total_price")
        val totalPrice: String? = null,
        @SerializedName("customer")
        val customer: CustomerRequest? = null,
        val icon: String? = null
) {
    inner class HistoryLocation(
            var label: String? = null,
            @SerializedName("address_text")
            var addressText: String? = null,
            var latitude: String? = null,
            var longitude: String? = null,
            var landmark: String? = null
    )
}

