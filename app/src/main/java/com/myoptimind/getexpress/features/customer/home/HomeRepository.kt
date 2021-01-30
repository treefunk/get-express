package com.myoptimind.getexpress.features.customer.home

import com.myoptimind.getexpress.features.customer.cart.data.CartLocation
import com.myoptimind.getexpress.features.customer.home.api.HomeService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeService: HomeService,
    private val appSharedPref: AppSharedPref
){
    fun getServices() = flow<Result<HomeService.ServicesResponse>> {
        val response = homeService.getServices()
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun initCart(
            cartTypeId: String,
            vehicleId: String,
            cartLocation: CartLocation?
    ) = flow {
        val response = homeService.initCart(
            cartTypeId,
                appSharedPref.getUserId(),
                vehicleId,
                cartLocation?.toJsonString()
        )
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}