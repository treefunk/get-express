package com.myoptimind.getexpress.features.customer.cart.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class CartLocation(
        val label: String = "",
        @SerializedName("address_text")
        val addressText: String = "",
        @SerializedName("latitude")
        val latitude: String = "",
        @SerializedName("longitude")
        val longitude: String = "",
        val landmark: String = ""
){
        companion object {
                fun fromPlace(place: Place): CartLocation {
                        return CartLocation(
                                place.name!!, place.address!!, place.latLng!!.latitude.toString(), place.latLng!!.longitude.toString(), ""
                        )
                }
        }
        fun toJsonString(): String {
                return JSONObject().apply {
                        put("label",label)
                        put("address_text",addressText)
                        put("latitude",latitude)
                        put("longitude",longitude)
                        put("landmark",landmark)
                }.toString()
        }

        fun toPlace(): Place {
                return Place.builder()
                        .setName(label)
                        .setAddress(addressText)
                        .setLatLng(LatLng(latitude.toDouble(),longitude.toDouble()))
                        .build()
        }


}