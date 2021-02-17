package com.myoptimind.get_express.features.rider.selected_customer_request

import com.myoptimind.get_express.features.rider.selected_customer_request.api.CustomerRequestService
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CustomerRequestRepository @Inject constructor(
    private val customerRequestService: CustomerRequestService
) {
    fun getCartInfo(
        cartId: String
    ) = flow {
        val response = customerRequestService.getCartInfo(cartId)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun acceptCustomerRequest(
            riderId: String,
            cartId: String
    ) = flow {
        val response = customerRequestService.acceptCustomerRequest(cartId, riderId)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun changeStatus(
            cartId: String,
            status: String
    ) = flow {
        val response = customerRequestService.changeCustomerRequestStatus(cartId,status)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun completeBooking(
            cartId: String,
            totalPrice: String,
            paymentStatus: String
    ) = flow {
        val response = customerRequestService.completeBooking(cartId,totalPrice,paymentStatus)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun sendRiderCurrentLocation(
        cartId: String,
        latitude: Double,
        longitude: Double
    ) = flow {
        val response = customerRequestService.sendRiderLocation(
            cartId,
            latitude,
            longitude
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)




}