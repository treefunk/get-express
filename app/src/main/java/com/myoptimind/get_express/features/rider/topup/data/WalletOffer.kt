package com.myoptimind.get_express.features.rider.topup.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalletOffer(
    val id: String,
    val name: String,
    val price: String,
    val description: String,
    @SerializedName("duration_in_hours")
    val durationInHours: String,
    @SerializedName("duration_label")
    val durationLabel: String,
): Parcelable