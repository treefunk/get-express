package com.myoptimind.get_express.features.customer.home.api

import com.myoptimind.get_express.features.customer.cart.data.GetCartInfoResponse
import com.myoptimind.get_express.features.customer.home.data.Service
import com.myoptimind.get_express.features.login.data.VehicleListItem
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface HomeService {



    @GET("services")
    suspend fun getServices(): ServicesResponse

    data class ServicesResponse(
        val data: List<Service>,
        val meta: MetaResponse
    )

    /**
     *  Vehicle list
     */

    @GET("vehicles")
    suspend fun getVehicles(): GetVehiclesResponse

    data class GetVehiclesResponse(
        val data: List<VehicleListItem>,
        val meta: MetaResponse
    )

    @POST("cart/initialize")
    @FormUrlEncoded
    suspend fun initCart(
            @Field("cart_type_id") cartTypeId: String,
            @Field("customer_id") customerId: String,
            @Field("vehicle_id") vehicleId: String,
            @Field("delivery_location") deliveryLocation: String? = null
    ): GetCartInfoResponse

}