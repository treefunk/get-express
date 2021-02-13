package com.myoptimind.get_express.features.customer.delivery.api

import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.customer.cart.data.GetCartInfoResponse
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.*

interface DeliveryService {

    @GET("vehicles/{vehicle_type_id}/delivery-details")
    suspend fun getDeliveryFormDetails(
            @Path("vehicle_type_id") vehicleTypeId: String
    ): DeliveryFormResponse

    data class DeliveryFormResponse(
            val data: DeliveryFormData,
            val meta: MetaResponse
    )

    data class DeliveryFormData(
            val categories: List<String>,
            @SerializedName("vehicle_weight_capacity")
            val vehicleWeightCapacity: String,
            @SerializedName("vehicle_weight_capacity_text")
            val vehicleWeightCapacityText: String,
    )

    @POST("cart/{cart_id}")
    @FormUrlEncoded
    suspend fun createDelivery(
            @Path("cart_id") cartId: String,
            @Field("notes") notes: String,
            @Field("category") category: String,
            @Field("distance") distance: String
    ): GetCartInfoResponse


}