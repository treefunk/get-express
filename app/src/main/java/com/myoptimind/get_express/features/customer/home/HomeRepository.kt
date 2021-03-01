package com.myoptimind.get_express.features.customer.home

import com.myoptimind.get_express.features.customer.cart.data.CartLocation
import com.myoptimind.get_express.features.customer.home.api.HomeService
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeService: HomeService,
    private val appSharedPref: AppSharedPref
){
    fun getServices() = flow<Result<HomeService.ServicesResponse>> {
        val response = homeService.getServices()
        emit(Result.Progress(isLoading = false))
        delay(100)
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
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects(false,true)
}