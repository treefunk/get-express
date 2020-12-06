package com.myoptimind.getexpress.features.login.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

data class VehicleListItem(
    val id: String,

    val name: String,

    val image: String
): BaseRemoteEntity()