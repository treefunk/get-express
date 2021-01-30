package com.myoptimind.getexpress.features.customer.pabili

import com.myoptimind.getexpress.features.customer.cart.data.ItemInPabili
import com.myoptimind.getexpress.features.customer.pabili.api.PabiliService
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
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
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}