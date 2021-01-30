package com.myoptimind.getexpress.features.rider.customer_requests_list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.getexpress.features.edit_profile.ProfileRepository
import com.myoptimind.getexpress.features.rider.customer_requests_list.api.CustomerRequestService
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CustomerRequestViewModel @ViewModelInject constructor(
        private val customerRequestRepository: CustomerRequestRepository,
        private val profileRepository: ProfileRepository
): ViewModel() {

    val customerRequestsResult: LiveData<Result<CustomerRequestService.GetCustomerRequestResponse>> get() = _customerRequestsResult
    private val _customerRequestsResult = MutableLiveData<Result<CustomerRequestService.GetCustomerRequestResponse>>()

    fun getCustomerRequests(){
        viewModelScope.launch(Dispatchers.IO){
            customerRequestRepository.getCustomerRequests().collect {
                _customerRequestsResult.postValue(it)
            }
        }
    }
}