package com.myoptimind.get_express.features.shared.api

import com.google.gson.annotations.SerializedName


data class MetaResponse(
    val message: String,
    var code: String,
    val status: Int,
    val total: Int? = null
)

data class TopUpMetaResponse(
    val message: String,
    var code: String,
    val status: Int,
    @SerializedName("topup_description")
    val topUpDescription: String
)