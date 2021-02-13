package com.myoptimind.get_express.features.rider.rider_history

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.get_express.features.rider.rider_history.api.RiderHistoryService
import kotlinx.coroutines.flow.collect
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.data.CartType
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class RiderHistoryViewModel @ViewModelInject constructor(
        val riderHistoryRepository: RiderHistoryRepository
): ViewModel() {


    val getRiderHistoryResult: LiveData<Result<RiderHistoryService.GetRiderHistoryResponse>> get() = _getRiderHistoryResult
    private val _getRiderHistoryResult = MutableLiveData<Result<RiderHistoryService.GetRiderHistoryResponse>>()

    val selectedCartType: LiveData<CartType> get() = _selectedCartType
    private val _selectedCartType = MutableLiveData<CartType>()


    init {
        getRiderHistory()
    }

    fun updatedSelectedCartType(cartType: CartType){
        _selectedCartType.value = cartType
    }

    fun getRiderHistory(cartType: CartType? = null) {
        viewModelScope.launch(IO){
            riderHistoryRepository.getRiderHistory(cartType?.id).collect {
                _getRiderHistoryResult.postValue(it)
                _selectedCartType.postValue(cartType)
            }
        }
    }
}