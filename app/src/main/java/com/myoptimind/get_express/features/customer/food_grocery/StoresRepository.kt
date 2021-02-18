package com.myoptimind.get_express.features.customer.food_grocery

import com.myoptimind.get_express.features.customer.food_grocery.api.StoresService
import com.myoptimind.get_express.features.customer.cart.data.CartLocation
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class StoresRepository @Inject constructor(
        private val storesService: StoresService
) {
    fun getStoresByService(
            serviceId: String,
            cartId: String?,
    ) = flow {
        val response = storesService.getStoresByService(serviceId,cartId)
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun getStoresByKeyword(
            keyword: String,
            serviceId: String,
            cartId: String?
    ) = flow {
        val response = storesService.getStoresByKeyword(keyword,serviceId,cartId)
        emit(Result.Success(response))
    }.applyDefaultEffects()


    fun getProductsByStore(
            storeId: String,
            category: String?,
            search: String?
    ) = flow {
        val response = storesService.getProductsByStore(
                storeId,
                category,
                search
        )
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun getCategoriesByStore(
        storeId: String
    ) = flow {
        val response = storesService.getCategoriesByStore(storeId)
        emit(Result.Success(response))
    }.applyDefaultEffects(true,true)

    fun addItemToCart(
            cartId: String,
            productId: String,
            quantity: String,
            notes: String,
            addOnIds: List<String>?,
            cartItemId: String?
    ) = flow {
        val response = storesService.addOrUpdateItem(
                cartId,
                productId,
                quantity,
                notes,
                addOnIds,
                cartItemId
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,false)

    fun EmptyThenAddItemToCart(
            cartId: String,
            productId: String,
            quantity: String,
            notes: String,
            addOnIds: List<String>?,
            cartItemId: String?
    ) = flow {
        val emptyCartResponse = storesService.emptyCart(cartId)
        if(emptyCartResponse.meta.status == 200){
            val response = storesService.addOrUpdateItem(
                    cartId,
                    productId,
                    quantity,
                    notes,
                    addOnIds,
                    cartItemId
            )
            emit(Result.Success(response))
        }
    }.applyDefaultEffects(false,false)

    fun finalizeCart(
        cartId: String,
        notes: String,
        pickupLocation: CartLocation? = null,
        deliveryLocation: CartLocation? = null,
        paymentType: String,
        statusChange: Boolean = true
    ) = flow {
        val response = storesService.finalizeCart(
                cartId,
                notes,
                pickupLocation?.toJsonString(),
                deliveryLocation?.toJsonString(),
                paymentType,
                if(statusChange) null else "false"
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,false)

    fun getCartInformation(
            cartId: String
    ) = flow {
        val response = storesService.getCartInformation(
                cartId
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)

    fun getCustomerActiveBooking(
            customerId: String,
    ) = flow {
        val response = storesService.getActiveBooking(
                customerId
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(true, false)

    fun cancelBooking(
            cartId: String
    ) = flow {
        val response = storesService.cancelCart(
                cartId
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,false)

    fun emptyCart(
            cartId: String
    ) = flow {
        val response = storesService.emptyCart(
                cartId
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}