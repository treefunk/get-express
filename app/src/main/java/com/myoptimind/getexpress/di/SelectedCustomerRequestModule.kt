package com.myoptimind.getexpress.di

import android.content.Context
import com.myoptimind.getexpress.features.CUSTOMER.home.api.HomeService
import com.myoptimind.getexpress.features.login.LoginRepository
import com.myoptimind.getexpress.features.login.api.LoginService
import com.myoptimind.getexpress.features.rider.selected_customer_request.CustomerRequestRepository
import com.myoptimind.getexpress.features.rider.selected_customer_request.api.CustomerRequestService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class SelectedCustomerRequestModule {

    @ActivityRetainedScoped
    @Provides
    fun provideCustomerRequestService(retrofit: Retrofit): CustomerRequestService {
        return retrofit.create(CustomerRequestService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideCustomerRequestRepository(customerRequestService: CustomerRequestService): CustomerRequestRepository {
        return CustomerRequestRepository(customerRequestService)
    }
}