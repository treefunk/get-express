package com.myoptimind.getexpress.features.customer.delivery

import com.myoptimind.getexpress.features.customer.delivery.api.DeliveryService
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeliveryRepository @Inject constructor(
        private val deliveryService: DeliveryService
) {

    fun getDeliveryForm(
            vehicleTypeId: String
    ) = flow {
        val response = deliveryService.getDeliveryFormDetails(vehicleTypeId)
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
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}