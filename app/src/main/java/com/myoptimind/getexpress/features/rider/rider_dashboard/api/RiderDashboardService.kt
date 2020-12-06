package com.myoptimind.getexpress.features.rider.rider_dashboard.api

import com.google.gson.annotations.SerializedName
import com.myoptimind.getexpress.features.rider.rider_dashboard.data.CustomerRequest
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RiderDashboardService {

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