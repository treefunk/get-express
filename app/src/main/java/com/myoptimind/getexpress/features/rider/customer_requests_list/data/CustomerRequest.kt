package com.myoptimind.getexpress.features.rider.customer_requests_list.data

import com.google.gson.annotations.SerializedName


/*
data class CustomerRequest (
        val customer: Customer,
        @SerializedName("delivery_location")
        val deliveryLocation: String,
        val pickupLocation: String,
        val totalPrice: String
)*/
data class CustomerRequest(
        @SerializedName("cart_id")
        val cartId: String,
        val customer: CustomerDetails,
        @SerializedName("order_info")
        val orderInfo: OrderInfo,
        @SerializedName("cart_meta")
        val cartMeta: CartMeta
){
        inner class CustomerDetails (
                @SerializedName("profile_picture")
                val profilePicture: String,
                @SerializedName("full_name")
                val fullName: String,
                @SerializedName("mobile_num")
                val mobileNum: String
        )

        inner class OrderInfo(
                @SerializedName("pickup_location_label")
                val pickupLocationLabel: String,
                @SerializedName("grand_total")
                val grandTotal: Int,
                @SerializedName("service_icon")
                val serviceIcon: String
        )

        inner class CartMeta(
                @SerializedName("cart_type_id")
                val cartTypeId: String,
                @SerializedName("service_type")
                val serviceType: String
        )
}