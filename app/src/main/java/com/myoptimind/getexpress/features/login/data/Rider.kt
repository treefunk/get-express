package com.myoptimind.getexpress.features.login.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

data class Rider(
        @SerializedName("identification_token")
        var identificationDocument: String = "",
        @SerializedName("is_admin_verified")
        var isAdminVerified: String = "",
        var vehicles: List<RiderVehicle> = ArrayList(),
        @SerializedName("active_vehicle")
        var activeVehicle: RiderVehicle = RiderVehicle(),
) : User(){

}