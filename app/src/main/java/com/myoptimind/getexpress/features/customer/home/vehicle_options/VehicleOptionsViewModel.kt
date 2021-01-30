package com.myoptimind.getexpress.features.customer.home.vehicle_options

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.getexpress.features.customer.home.vehicle_options.api.VehicleOptionsService
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class VehicleOptionsViewModel @ViewModelInject constructor(
        private val vehicleOptionsRepository: VehicleOptionsRepository
): ViewModel() {

    val vehicleOptionsResult: LiveData<Result<VehicleOptionsService.VehicleByServiceResponse>>
        get() = _vehicleOptionsResult
    private val _vehicleOptionsResult = MutableLiveData<Result<VehicleOptionsService.VehicleByServiceResponse>>()

    fun getVehicleOptions(
            serviceId: String
    ){
        viewModelScope.launch(IO) {
            vehicleOptionsRepository.getVehiclesByService(serviceId).collect {
                _vehicleOptionsResult.postValue(it)
            }
        }
    }
}