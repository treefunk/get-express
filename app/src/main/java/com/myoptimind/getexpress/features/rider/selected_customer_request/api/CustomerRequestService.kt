package com.myoptimind.getexpress.features.rider.selected_customer_request.api

import com.myoptimind.getexpress.features.customer.cart.data.Cart
import com.myoptimind.getexpress.features.customer.cart.data.GetCartInfoResponse
//import com.myoptimind.getexpress.features.customer.cart.data.CartForItems
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import retrofit2.http.*

interface CustomerRequestService {

    @GET("cart/{cart_id}/info")
    suspend fun getCartInfo(
        @Path("cart_id") cartId: String
    ): GetCartInfoResponse

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



    @POST("cart/{cart_id}/rider-location")
    @FormUrlEncoded
    suspend fun sendRiderLocation(
        @Path("cart_id") cartId: String,
        @Field("rider_current_location_latitude") latitude: Double,
        @Field("rider_current_location_longitude") longitude: Double
    ): GetCartInfoResponse

    @POST("cart/{cart_id}/complete")
    @FormUrlEncoded
    suspend fun completeBooking(
            @Path("cart_id") cartId: String,
            @Field("total_price") totalPrice: String,
            @Field("payment_status") paymentStatus: String
    ): GetCartInfoResponse



}