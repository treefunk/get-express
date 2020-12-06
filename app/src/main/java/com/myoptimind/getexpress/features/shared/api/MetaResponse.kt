package com.myoptimind.getexpress.features.shared.api

data class MetaResponse(
    val message: String,
    val code: String,
    val status: Int,
    val total: Int? = null
)