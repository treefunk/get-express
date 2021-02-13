package com.myoptimind.get_express.features.customer.home.vehicle_options.api

import com.myoptimind.get_express.features.customer.home.vehicle_options.data.VehicleOption
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface VehicleOptionsService {

    @GET("services/{service_id}/vehicles")
    suspend fun getVehiclesByService(
            @Path("service_id") serviceId: String
    ): VehicleByServiceResponse

    data class VehicleByServiceResponse(
            val data: List<VehicleOption>,
            val meta: MetaResponse
    )

}