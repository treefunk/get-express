package com.myoptimind.get_express.features.customer.whats_new

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.myoptimind.get_express.features.customer.whats_new.api.WhatsNewService
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class WhatsNewViewModel @ViewModelInject constructor(
        private val whatsNewRepository: WhatsNewRepository
): ViewModel() {
    val whatsNewListResult = whatsNewRepository.getAllNews().asLiveData().asFlow()

    init {
    }

//    private val _whatsNewListResult = MutableLiveData<Result<WhatsNewService.NewsResponse>>()
}