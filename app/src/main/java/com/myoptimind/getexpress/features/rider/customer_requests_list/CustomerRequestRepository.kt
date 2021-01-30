package com.myoptimind.getexpress.features.rider.customer_requests_list

import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.rider.customer_requests_list.api.CustomerRequestService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import com.myoptimind.getexpress.features.shared.api.Result


class CustomerRequestRepository @Inject constructor(
        private val customerRequestService: CustomerRequestService,
        private val profileService: ProfileService,
        private val appSharedPref: AppSharedPref
){
    fun getCustomerRequests(
    ) = flow<Result<CustomerRequestService.GetCustomerRequestResponse>> {
        val response = profileService.getRiderProfile(appSharedPref.getUserId())
        emit(Result.Success(customerRequestService.getCustomerRequests(response.data.activeVehicle.vehicleId,appSharedPref.getUserId())))
    }.applyDefaultEffects(true,false)
}