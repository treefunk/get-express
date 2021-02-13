package com.myoptimind.get_express.features.customer.whats_new.api

import com.myoptimind.get_express.features.customer.whats_new.data.WhatsNew
import com.myoptimind.get_express.features.shared.api.MetaResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface WhatsNewService {

    @GET("news")
    suspend fun getAllNews(): NewsResponse

    data class NewsResponse(
            val data: List<WhatsNew>,
            val meta: MetaResponse
    )


    @GET("news/{id}")
    suspend fun getSingleNews(
            @Path("id") id: String
    ): SingleNewsResponse

    data class SingleNewsResponse(
            val data: WhatsNew,
            val meta: MetaResponse
    )

}