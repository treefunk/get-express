package com.myoptimind.get_express.features.rider.customer_requests_list

import com.myoptimind.get_express.features.db.DeclinedRequest
import com.myoptimind.get_express.features.db.DeclinedRequestDao
import com.myoptimind.get_express.features.edit_profile.api.ProfileService
import com.myoptimind.get_express.features.rider.customer_requests_list.api.CustomerRequestService
import com.myoptimind.get_express.features.rider.topup.api.RiderTopupService
import com.myoptimind.get_express.features.shared.AppSharedPref
import com.myoptimind.get_express.features.shared.api.MetaResponse
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import com.myoptimind.get_express.features.shared.api.Result


class CustomerRequestRepository @Inject constructor(
    private val customerRequestService: CustomerRequestService,
    private val profileService: ProfileService,
    private val topupService: RiderTopupService,
    private val declinedRequestDao: DeclinedRequestDao,
    private val appSharedPref: AppSharedPref
){
    fun getCustomerRequests(
    ) = flow {
        val rider = profileService.getRiderProfile(appSharedPref.getUserId()).data
        emit(Result.Success(rider))
        emit(Result.Success(topupService.getRemainingBalance(rider.id)))
        topupService.getWalletTransactionHistory(rider.id)
        emit(Result.Success(topupService.getWalletTransactionHistory(rider.id)))
        val customerRequests = customerRequestService.getCustomerRequests(rider.activeVehicle.vehicleId,appSharedPref.getUserId())
        val declinedRequests = declinedRequestDao.getAllDeclinedRequests()
        val filteredCustomerRequests = customerRequests.data.filter { it ->
            declinedRequests.map{ declinedRequest -> declinedRequest.cartId }.contains(it.cartId).not()
        }
        customerRequests.data = filteredCustomerRequests
        emit(Result.Success(customerRequests))
    }.applyDefaultEffects(true,false)

    suspend fun addDeclinedRequest(
        declinedRequest: DeclinedRequest
    ){
        declinedRequestDao.addDeclinedRequest(declinedRequest)
    }

    suspend fun clearDeclinedRequests(){
        declinedRequestDao.deleteAllDeclinedRequests()
    }
}