package com.myoptimind.getexpress.features.CUSTOMER.home.api

import com.myoptimind.getexpress.features.CUSTOMER.home.data.Service
import com.myoptimind.getexpress.features.login.data.VehicleListItem
import com.myoptimind.getexpress.features.shared.api.MetaResponse
import retrofit2.http.GET

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

}