package com.myoptimind.get_express.features.customer.home.vehicle_options

import com.myoptimind.get_express.features.customer.home.vehicle_options.api.VehicleOptionsService
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VehicleOptionsRepository @Inject constructor(
        private val vehicleOptionsService: VehicleOptionsService
) {
    fun getVehiclesByService(
            serviceId: String
    ) = flow {
        val response = vehicleOptionsService.getVehiclesByService(serviceId)
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects()
}