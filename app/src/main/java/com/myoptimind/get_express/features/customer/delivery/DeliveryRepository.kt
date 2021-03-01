package com.myoptimind.get_express.features.customer.delivery

import com.myoptimind.get_express.features.customer.delivery.api.DeliveryService
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeliveryRepository @Inject constructor(
        private val deliveryService: DeliveryService
) {

    fun getDeliveryForm(
            vehicleTypeId: String
    ) = flow {
        val response = deliveryService.getDeliveryFormDetails(vehicleTypeId)
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(true,false)

    fun createDelivery(
            cartId: String,
            notes: String,
            category: String,
            distance: String
    ) = flow {
        val response = deliveryService.createDelivery(
                cartId,notes,category,distance
        )
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}