package com.myoptimind.get_express.features.customer.whats_new.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WhatsNew(
        val id: String,
        val title: String,
        val category: String,
        val body: String,
        @SerializedName("banner_image")
        val bannerImage: String
): BaseRemoteEntity(), Parcelable