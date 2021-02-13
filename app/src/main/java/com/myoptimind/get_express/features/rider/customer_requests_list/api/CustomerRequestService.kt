package com.myoptimind.get_express.features.rider.customer_requests_list.api

import com.google.gson.annotations.SerializedName
import com.myoptimind.get_express.features.rider.customer_requests_list.data.CustomerRequest
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomerRequestService {

    @GET("cart/vehicles/{vehicle_id}")
    suspend fun getCustomerRequests(
            @Path("vehicle_id") vehicleId: String,
            @Query("rider_id") riderId: String? = null
    ): GetCustomerRequestResponse

    data class GetCustomerRequestResponse(
            val data: List<CustomerRequest>,
            @SerializedName("active_cart_id")
            val activeCartId: String,
            val meta: MetaResponse
    )
}