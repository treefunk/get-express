package com.myoptimind.getexpress.features.login.data

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.shared.api.BaseRemoteEntity

data class Customer(
    var addresses: List<Address> = listOf()
): User()