package com.myoptimind.get_express.features.customer.home.vehicle_options.data

import com.google.gson.annotations.SerializedName

data class VehicleOption(
        @SerializedName("vehicle_id")
        val vehicleId: String,
        val name: String,
        val image: String
)