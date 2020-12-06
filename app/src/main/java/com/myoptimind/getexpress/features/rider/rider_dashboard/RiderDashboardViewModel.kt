package com.myoptimind.getexpress.features.rider.rider_dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.myoptimind.getexpress.features.edit_profile.ProfileRepository
import com.myoptimind.getexpress.features.rider.rider_dashboard.api.RiderDashboardService
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.delay
import timber.log.Timber

class RiderDashboardViewModel @ViewModelInject constructor(
        private val riderDashboardRepository: RiderDashboardRepository,
        private val profileRepository: ProfileRepository
): ViewModel() {


//    val customerRequestsResult = riderDashboardRepository.getCustomerRequests().asLiveData()
    val customerRequestsResult: LiveData<Result<RiderDashboardService.GetCustomerRequestResponse>> get() = _customerRequestsResult
    private val _customerRequestsResult = MutableLiveData<Result<RiderDashboardService.GetCustomerRequestResponse>>()




    fun getCustomerRequests(){
        viewModelScope.launch(IO){
            riderDashboardRepository.getCustomerRequests().collect {
                _customerRequestsResult.postValue(it)
            }
        }
    }


}