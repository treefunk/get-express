package com.myoptimind.get_express.features.customer.home.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.shared.api.BaseRemoteEntity


data class Service(
    val id: String,
    val type: String,
    val icon: String,
    val label: String,
    val description: String,
    @SerializedName("has_cms_access")
    val hasCmsAccess: String,
): BaseRemoteEntity()