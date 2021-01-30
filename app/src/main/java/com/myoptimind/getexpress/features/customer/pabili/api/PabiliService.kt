package com.myoptimind.getexpress.features.customer.pabili.api

import com.myoptimind.getexpress.features.customer.cart.data.GetCartInfoResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface PabiliService {

    @POST("cart/{cart_id}")
    @FormUrlEncoded
    suspend fun createPabili(
            @Path("cart_id") cartId: String,
            @Field("estimate_total_amount") estimateTotalAmount: String,
            @Field("item_name[]") itemNames: List<String>,
            @Field("quantity[]") itemQuantities: List<String>
    ): GetCartInfoResponse
}