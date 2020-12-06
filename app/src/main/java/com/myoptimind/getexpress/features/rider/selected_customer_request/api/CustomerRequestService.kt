package com.myoptimind.getexpress.features.rider.selected_customer_request.api

import com.myoptimind.getexpress.features.rider.selected_customer_request.data.Cart
//import com.myoptimind.getexpress.features.rider.selected_customer_request.data.CartForItems
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import retrofit2.http.*

interface CustomerRequestService {

    @GET("cart/{cart_id}/info")
    suspend fun getCartInfo(
        @Path("cart_id") cartId: String
    ): GetCartInfoResponse

    data class GetCartInfoResponse(
        val data: Cart,
        val meta: MetaResponse
    )

    @POST("cart/{cart_id}/accept/riders/{rider_id}")
    suspend fun acceptCustomerRequest(
            @Path("cart_id") cartId: String,
            @Path("rider_id") riderId: String
    ): GetCartInfoResponse

    @POST("cart/{cart_id}/status")
    @FormUrlEncoded
    suspend fun changeCustomerRequestStatus(
            @Path("cart_id") cartId: String,
            @Field("status") status:String
    ): GetCartInfoResponse
}