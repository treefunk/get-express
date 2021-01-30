package com.myoptimind.getexpress.features.rider.rider_history

import com.myoptimind.getexpress.features.rider.rider_history.api.RiderHistoryService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RiderHistoryRepository @Inject constructor(
    private val riderHistoryService: RiderHistoryService,
    val appSharedPref: AppSharedPref
){
    fun getRiderHistory(serviceId: String?) = flow {
        val userId = appSharedPref.getUserId()
        val response = if(serviceId.isNullOrBlank()){
            riderHistoryService.getRiderHistory(userId)
        }else{
            riderHistoryService.getRiderHistoryByService(appSharedPref.getUserId(),serviceId)
        }
        emit(Result.Success(response))
    }.applyDefaultEffects()
}