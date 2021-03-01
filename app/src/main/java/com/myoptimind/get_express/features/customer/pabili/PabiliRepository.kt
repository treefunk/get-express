package com.myoptimind.get_express.features.customer.pabili

import com.myoptimind.get_express.features.customer.cart.data.ItemInPabili
import com.myoptimind.get_express.features.customer.pabili.api.PabiliService
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PabiliRepository @Inject constructor(
        private val pabiliService: PabiliService
){

    fun createPabili(
            cartId: String,
            estimateTotalAmount: Double,
            pabiliItems: List<ItemInPabili>
    ) = flow {
        val response = pabiliService.createPabili(
                cartId,
                estimateTotalAmount.toString(),
                pabiliItems.map { it.itemName },
                pabiliItems.map { it.quantity }
        )
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}