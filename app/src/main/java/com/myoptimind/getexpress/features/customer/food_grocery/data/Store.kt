package com.myoptimind.getexpress.features.customer.food_grocery.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

data class Store(
        val id: String,
        @SerializedName("service_id")
        val serviceId: String,
        val name: String,
        val email: String,
        val banner: String,
        val category: String,
        @SerializedName("location_text")
        val locationText: String,
        val coordinates: Coordinates,
        val image: String
): BaseRemoteEntity()

data class Coordinates(
        val latitude: String,
        val longitude: String
)