package com.myoptimind.getexpress.features.rider.rider_dashboard

import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.rider.rider_dashboard.api.RiderDashboardService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.myoptimind.getexpress.features.shared.applyDefaultEffects
import com.myoptimind.getexpress.features.shared.api.Result


class RiderDashboardRepository @Inject constructor(
        private val riderDashboardService: RiderDashboardService,
        private val profileService: ProfileService,
        private val appSharedPref: AppSharedPref
){
    fun getCustomerRequests(
    ) = flow<Result<RiderDashboardService.GetCustomerRequestResponse>> {
        val response = profileService.getRiderProfile(appSharedPref.getUserId())
        emit(Result.Success(riderDashboardService.getCustomerRequests(response.data.activeVehicle.vehicleId,appSharedPref.getUserId())))
    }.applyDefaultEffects(true,false)
}