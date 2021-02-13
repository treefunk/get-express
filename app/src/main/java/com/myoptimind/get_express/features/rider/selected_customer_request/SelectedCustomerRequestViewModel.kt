package com.myoptimind.get_express.features.rider.selected_customer_request

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.get_express.features.customer.cart.data.CartStatus
import com.myoptimind.get_express.features.customer.cart.data.GetCartInfoResponse
import com.myoptimind.get_express.features.shared.AppSharedPref
import kotlinx.coroutines.Dispatchers.IO
import com.myoptimind.get_express.features.shared.api.Result

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SelectedCustomerRequestViewModel @ViewModelInject constructor(
        private val appSharedPref: AppSharedPref,
        private val customerRequestRepository: CustomerRequestRepository
): ViewModel() {

    val cartInfoResult: LiveData<Result<GetCartInfoResponse>> get() = _cartInfoResult
    private val _cartInfoResult = MutableLiveData<Result<GetCartInfoResponse>>()

    val acceptCustomerRequestResult: LiveData<Result<GetCartInfoResponse>> get() = _acceptCustomerRequestResult
    private val _acceptCustomerRequestResult = MutableLiveData<Result<GetCartInfoResponse>>()

    val riderLocationResult: LiveData<Result<GetCartInfoResponse>> get() = _riderLocationResult
    private val _riderLocationResult = MutableLiveData<Result<GetCartInfoResponse>>()

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
            cartStatus: CartStatus
    ){
        viewModelScope.launch(IO){
            customerRequestRepository.changeStatus(cartId,cartStatus.key).collect {
                _cartInfoResult.postValue(it)
            }
        }
    }

    fun completeBooking(
            cartId: String,
            totalPrice: String,
            paymentStatus: String
    ){
        viewModelScope.launch(IO){
            customerRequestRepository.completeBooking(cartId,totalPrice,paymentStatus).collect {
                _cartInfoResult.postValue(it)
            }
        }
    }

/*    fun sendRiderLocationUpdates(
        cartId: String,
        latitude: Double,
        longitude: Double
    ){
        viewModelScope.launch(IO){
            customerRequestRepository.sendRiderCurrentLocation(
                cartId, latitude, longitude
            ).collect {
                _riderLocationResult.postValue(it)
            }
        }
    }*/
}