package com.myoptimind.get_express.features.rider.rider_history.api

import com.myoptimind.get_express.features.rider.rider_history.data.RiderHistory
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RiderHistoryService {

    @GET("cart/{user_type}/{user_id}/history")
    suspend fun getRiderHistory(
            @Path("user_id") userId: String,
            @Path("user_type") userType: String
    ): GetRiderHistoryResponse

    data class GetRiderHistoryResponse(
            val data: List<RiderHistory>,
            val meta: MetaResponse
    )

    @GET("cart/{user_type}/{user_id}/history/services/{service_id}")
    suspend fun getRiderHistoryByService(
            @Path("user_id") userId: String,
            @Path("service_id") serviceId: String,
            @Path("user_type") userType: String
    ): GetRiderHistoryResponse
}