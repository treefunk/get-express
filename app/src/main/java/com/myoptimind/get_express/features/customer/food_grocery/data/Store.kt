package com.myoptimind.get_express.features.customer.food_grocery.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Store(
        val id: String,
        @SerializedName("service_id")
        val serviceId: String,
        val name: String,
        val email: String,
        val banner: String,
        val category: String,
        @SerializedName("store_availability")
        val storeAvailability: String,
        @SerializedName("store_schedule")
        val storeSchedule: String,
        @SerializedName("contact_numbers")
        val contactNumbers: String,
        val about: String,
        @SerializedName("location_text")
        val locationText: String,
        val coordinates: Coordinates,
        val image: String
): BaseRemoteEntity(), Parcelable {

        val latLng: LatLng
                get() = LatLng(this.coordinates.latitude.toDouble(),this.coordinates.longitude.toDouble())

}

@Parcelize
data class Coordinates(
        val latitude: String,
        val longitude: String
): Parcelable