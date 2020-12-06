package com.myoptimind.getexpress.di

import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.rider.rider_dashboard.RiderDashboardRepository
import com.myoptimind.getexpress.features.rider.rider_dashboard.api.RiderDashboardService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class RiderDashboardModule {

    @ActivityRetainedScoped
    @Provides
    fun provideRiderDashboardService(retrofit: Retrofit): RiderDashboardService {
        return retrofit.create(RiderDashboardService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideRiderDashboardRepository(
        riderDashboardService: RiderDashboardService,
        profileService: ProfileService,
        appSharedPref: AppSharedPref
    ): RiderDashboardRepository {
        return RiderDashboardRepository(
            riderDashboardService,
            profileService,
            appSharedPref
        )
    }

}
