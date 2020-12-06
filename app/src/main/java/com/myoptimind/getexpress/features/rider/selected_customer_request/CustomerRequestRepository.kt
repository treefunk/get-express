package com.myoptimind.getexpress.features.rider.selected_customer_request

import com.myoptimind.getexpress.features.rider.selected_customer_request.api.CustomerRequestService
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
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
}