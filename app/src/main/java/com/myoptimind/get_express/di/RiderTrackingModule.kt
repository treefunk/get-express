package com.myoptimind.get_express.di

import com.myoptimind.get_express.features.rider.selected_customer_request.CustomerRequestRepository
import com.myoptimind.get_express.features.rider.selected_customer_request.api.CustomerRequestService

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import retrofit2.Retrofit


@Module
@InstallIn(ServiceComponent::class)
class RiderTrackingModule {

    @ServiceScoped
    @Provides
    fun provideCustomerRequestService(retrofit: Retrofit): CustomerRequestService {
        return retrofit.create(CustomerRequestService::class.java)
    }

    @ServiceScoped
    @Provides
    fun provideCustomerRequestRepository(customerRequestService: CustomerRequestService): CustomerRequestRepository {
        return CustomerRequestRepository(customerRequestService)
    }


}