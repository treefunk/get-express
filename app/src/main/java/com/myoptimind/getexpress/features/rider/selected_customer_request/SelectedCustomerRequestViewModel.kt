package com.myoptimind.getexpress.features.rider.selected_customer_request

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.getexpress.features.rider.selected_customer_request.api.CustomerRequestService
import com.myoptimind.getexpress.features.rider.selected_customer_request.data.RiderCartStatus
import com.myoptimind.getexpress.features.shared.AppSharedPref
import kotlinx.coroutines.Dispatchers.IO
import com.myoptimind.getexpress.features.shared.api.Result

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SelectedCustomerRequestViewModel @ViewModelInject constructor(
        private val appSharedPref: AppSharedPref,
        private val customerRequestRepository: CustomerRequestRepository
): ViewModel() {

    val cartInfoResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _cartInfoResult
    private val _cartInfoResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    val acceptCustomerRequestResult: LiveData<Result<CustomerRequestService.GetCartInfoResponse>> get() = _acceptCustomerRequestResult
    private val _acceptCustomerRequestResult = MutableLiveData<Result<CustomerRequestService.GetCartInfoResponse>>()

    fun initCartInfo(
        cartId: String
    ){
        viewModelScope.launch(IO){
            customerRequestRepository.getCartInfo(cartId).collect {
                _cartInfoResult.postValue(it)
            }
        }
    }

    fun acceptCustomerRequest(cartId: String){
        viewModelScope.launch(IO){
            customerRequestRepository.acceptCustomerRequest(appSharedPref.getUserId(),cartId).collect {
                _cartInfoResult.postValue(it)
            }
        }
    }

    fun updateStatusCustomerRequest(
            cartId: String,
            cartStatus: RiderCartStatus
    ){
        viewModelScope.launch(IO){
            customerRequestRepository.changeStatus(cartId,cartStatus.key).collect {
                _cartInfoResult.postValue(it)
            }
        }
    }
}