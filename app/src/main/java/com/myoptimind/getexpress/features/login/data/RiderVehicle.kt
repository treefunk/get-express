package com.myoptimind.getexpress.features.login.data

import com.google.gson.annotations.SerializedName

data class RiderVehicle(
    var id: String = "",
    @SerializedName("vehicle_type")
    var vehicleType: String = "",
    @SerializedName("vehicle_id")
    var vehicleId: String = "",
    @SerializedName("vehicle_model")
    var vehicleModel: String = "",
    @SerializedName("plate_number")
    var plateNumber: String = "",
    @SerializedName("is_active")
    var isActive: String = ""
)