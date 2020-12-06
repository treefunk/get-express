package com.myoptimind.getexpress.features.rider.rider_history.api

import com.myoptimind.getexpress.features.rider.rider_history.data.RiderHistory
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface RiderHistoryService {

    @GET("cart/rider_id/{user_id}/history")
    suspend fun getRiderHistory(
            @Path("user_id") userId: String
    ): GetRiderHistoryResponse

    data class GetRiderHistoryResponse(
            val data: List<RiderHistory>,
            val meta: MetaResponse
    )
}