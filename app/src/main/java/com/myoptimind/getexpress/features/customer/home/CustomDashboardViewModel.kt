package com.myoptimind.getexpress.features.customer.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myoptimind.getexpress.features.customer.home.api.HomeService
import com.myoptimind.getexpress.features.rider.selected_customer_request.api.CustomerRequestService
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.myoptimind.getexpress.features.shared.api.Result

class CustomDashboardViewModel @ViewModelInject constructor(
        private val homeRepository: HomeRepository
): ViewModel() {


}