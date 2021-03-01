package com.myoptimind.get_express.features.rider.rider_history

import com.myoptimind.get_express.features.login.data.UserType
import com.myoptimind.get_express.features.rider.rider_history.api.RiderHistoryService
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RiderHistoryRepository @Inject constructor(
    private val riderHistoryService: RiderHistoryService,
    val appSharedPref: AppSharedPref
){
    fun getRiderHistory(serviceId: String?) = flow {
        val userId = appSharedPref.getUserId()
        val idKey = when(appSharedPref.getUserType()){
            UserType.CUSTOMER -> "customer_id"
            UserType.RIDER -> "rider_id"
        }
        val response = if(serviceId.isNullOrBlank()){

            riderHistoryService.getRiderHistory(userId,idKey)
        }else{
            riderHistoryService.getRiderHistoryByService(appSharedPref.getUserId(),serviceId,idKey)
        }
        emit(Result.Progress(isLoading = false))
        delay(100)
        emit(Result.Success(response))
    }.applyDefaultEffects()
}