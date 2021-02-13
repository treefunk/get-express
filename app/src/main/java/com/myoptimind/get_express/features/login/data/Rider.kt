package com.myoptimind.get_express.features.login.data

import com.google.gson.annotations.SerializedName

data class Rider(
        @SerializedName("identification_token")
        var identificationDocument: String = "",
        @SerializedName("is_admin_verified")
        var isAdminVerified: String = "",
        @SerializedName("booking_available_until")
        var bookingAvailableUntil: String = "",
        @SerializedName("cash_on_hand")
        var cashOnHand: String = "",
        @SerializedName("all_vehicles")
        var vehicles: List<RiderVehicle> = ArrayList(),
        @SerializedName("active_vehicle")
        var activeVehicle: RiderVehicle = RiderVehicle(),
        @SerializedName("unverified_vehicles")
        var unverifiedVehicles: List<RiderVehicle> = ArrayList()
) : User(){

}