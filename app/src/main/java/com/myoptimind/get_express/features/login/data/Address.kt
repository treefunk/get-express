package com.myoptimind.get_express.features.login.data

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(
    val id: String,
    @SerializedName("customer_id")
    val customerId: String,

    val label: String,

    @SerializedName("full_address")
    val fullAddress: String,

    val latitude: String,
    val longitude: String,
): Parcelable, BaseRemoteEntity() {
    fun toPlace(): Place =
            Place.builder()
                    .setAddress(fullAddress)
                    .setName(label)
                    .setLatLng(LatLng(latitude.toDouble(),longitude.toDouble()))
                    .build()
}