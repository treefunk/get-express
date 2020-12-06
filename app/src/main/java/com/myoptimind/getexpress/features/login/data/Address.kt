package com.myoptimind.getexpress.features.login.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

data class Address(
    val id: String,
    @SerializedName("customer_id")
    val customerId: String,

    val label: String,

    @SerializedName("full_address")
    val fullAddress: String,

    val latitude: String,
    val longitude: String,
): BaseRemoteEntity()