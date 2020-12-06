package com.myoptimind.getexpress.features.rider.selected_customer_request.data

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import com.myoptimind.getexpress.features.rider.rider_dashboard.data.CustomerRequest
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

open class Cart(
        val id: String = "",

        @SerializedName("cart_type_id")
        val cartTypeId: String = "",

        val customer: CustomerRequest,

        @SerializedName("vehicle_id")
        val vehicleId: String = "",

        val status: String = "",

        val payload: String = "",

        @SerializedName("rider_id")
        val riderId: String = "",

        val notes: String = "",

        @SerializedName("pickup_location")
        val pickUpLocation: CartLocation = CartLocation(),

        @SerializedName("delivery_location")
        val deliveryLocation: CartLocation = CartLocation(),

        @SerializedName("payment_method")
        val paymentMethod: String = "",

        @SerializedName("transaction_id")
        val transactionId: String = "",

        @SerializedName("payment_status")
        val paymentStatus: String = "",

        @SerializedName("total_price")
        val totalPrice: String = "",

        @SerializedName("service_type")
        val serviceType: String = "",

        val basket: Any = Any()

) : BaseRemoteEntity()

interface CartItem
