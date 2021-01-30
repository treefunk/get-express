package com.myoptimind.getexpress.di

import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.rider.customer_requests_list.CustomerRequestRepository
import com.myoptimind.getexpress.features.rider.customer_requests_list.api.CustomerRequestService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class CustomerRequestModule {

    @ActivityRetainedScoped
    @Provides
    fun provideRiderDashboardService(retrofit: Retrofit): CustomerRequestService {
        return retrofit.create(CustomerRequestService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideRiderDashboardRepository(
            riderDashboardService: CustomerRequestService,
            profileService: ProfileService,
            appSharedPref: AppSharedPref
    ): CustomerRequestRepository {
        return CustomerRequestRepository(
            riderDashboardService,
            profileService,
            appSharedPref
        )
    }

}
