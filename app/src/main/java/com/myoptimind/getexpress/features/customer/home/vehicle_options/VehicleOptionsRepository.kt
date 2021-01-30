package com.myoptimind.getexpress.features.customer.home.vehicle_options

import com.myoptimind.getexpress.features.customer.home.vehicle_options.api.VehicleOptionsService
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VehicleOptionsRepository @Inject constructor(
        private val vehicleOptionsService: VehicleOptionsService
) {
    fun getVehiclesByService(
            serviceId: String
    ) = flow {
        val response = vehicleOptionsService.getVehiclesByService(serviceId)
        emit(Result.Success(response))
    }.applyDefaultEffects()
}