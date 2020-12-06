package com.myoptimind.getexpress.features.rider.rider_history

import com.myoptimind.getexpress.features.rider.rider_history.api.RiderHistoryService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import com.myoptimind.getexpress.features.shared.api.Result
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RiderHistoryRepository @Inject constructor(
    val riderHistoryService: RiderHistoryService,
    val appSharedPref: AppSharedPref
){

    fun getRiderHistory() = flow {
        val response = riderHistoryService.getRiderHistory(appSharedPref.getUserId())
        emit(Result.Success(response))
    }.applyDefaultEffects()


}