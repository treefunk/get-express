package com.myoptimind.getexpress.features.rider.rider_history

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.getexpress.features.rider.rider_history.api.RiderHistoryService
import kotlinx.coroutines.flow.collect
import com.myoptimind.getexpress.features.shared.api.Result
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class RiderHistoryViewModel @ViewModelInject constructor(
        val riderHistoryRepository: RiderHistoryRepository
): ViewModel() {


    val getRiderHistoryResult: LiveData<Result<RiderHistoryService.GetRiderHistoryResponse>> get() = _getRiderHistoryResult
    private val _getRiderHistoryResult = MutableLiveData<Result<RiderHistoryService.GetRiderHistoryResponse>>()

    fun getRiderHistory() {
        viewModelScope.launch(IO){
            riderHistoryRepository.getRiderHistory().collect {
                _getRiderHistoryResult.postValue(it)
            }
        }
    }
}