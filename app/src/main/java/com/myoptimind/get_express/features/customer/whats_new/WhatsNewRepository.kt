package com.myoptimind.get_express.features.customer.whats_new

import com.myoptimind.get_express.features.customer.whats_new.api.WhatsNewService
import com.myoptimind.get_express.features.shared.api.Result
import com.myoptimind.get_express.features.shared.applyDefaultEffects
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WhatsNewRepository @Inject constructor(
        private val whatsNewService: WhatsNewService
) {
    fun getAllNews() = flow{
        val response = whatsNewService.getAllNews()
        emit(Result.Success(response))
    }.applyDefaultEffects()

    fun getSingleNews(
            id: String
    ) = flow {
        val response = whatsNewService.getSingleNews(id)
        emit(Result.Success(response))
    }
}