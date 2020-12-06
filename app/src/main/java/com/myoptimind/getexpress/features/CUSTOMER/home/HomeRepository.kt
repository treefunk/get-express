package com.myoptimind.getexpress.features.CUSTOMER.home

import com.myoptimind.getexpress.features.CUSTOMER.home.api.HomeService
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeService: HomeService
){
    fun getServices() = flow<Result<HomeService.ServicesResponse>> {
        val response = homeService.getServices()
        emit(Result.Success(response))
    }.applyDefaultEffects()
}