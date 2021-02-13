package com.myoptimind.get_express.features.rider.customer_requests_list

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
        private val appSharedPref: AppSharedPref
){
    fun getCustomerRequests(
    ) = flow {
        val rider = profileService.getRiderProfile(appSharedPref.getUserId()).data
        emit(Result.Success(rider))
        emit(Result.Success(topupService.getRemainingBalance(rider.id)))
        topupService.getWalletTransactionHistory(rider.id)
        emit(Result.Success(topupService.getWalletTransactionHistory(rider.id)))
/*        if(rider.bookingAvailableUntil.isBlank()){
            emit(
                    Result.Success(
                            CustomerRequestService.GetCustomerRequestResponse(ArrayList(),"",
                                    MetaResponse("not enough credits","expired",211,0)
                            )
                    )
            )
        }else{*/
            emit(Result.Success(customerRequestService.getCustomerRequests(rider.activeVehicle.vehicleId,appSharedPref.getUserId())))
//        }
    }.applyDefaultEffects(true,false)
}