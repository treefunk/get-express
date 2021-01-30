package com.myoptimind.getexpress.features.customer.delivery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import com.myoptimind.getexpress.features.customer.delivery.api.DeliveryService
import com.myoptimind.getexpress.features.login.data.VehicleType
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DeliveryViewModel @ViewModelInject constructor(
        private val deliveryRepository: DeliveryRepository
): ViewModel() {

    val deliveryFormDetails:LiveData<Result<DeliveryService.DeliveryFormResponse>> get() = _deliveryFormDetails
    private val _deliveryFormDetails = MutableLiveData<Result<DeliveryService.DeliveryFormResponse>>()

    var senderAddress: Place? = null
    var receiverAddress: Place? = null

    fun getDeliveryFormDetails(
            vehicleTypeId: String
    ){
        viewModelScope.launch(IO) {
            deliveryRepository.getDeliveryForm(vehicleTypeId).collect {
                _deliveryFormDetails.postValue(it)
            }
        }
    }
}