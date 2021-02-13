package com.myoptimind.get_express.di

import com.myoptimind.get_express.features.rider.rider_history.RiderHistoryRepository
import com.myoptimind.get_express.features.rider.rider_history.api.RiderHistoryService
import com.myoptimind.get_express.features.shared.AppSharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class RiderHistoryModule {

    @ActivityRetainedScoped
    @Provides
    fun provideRiderHistoryService(retrofit: Retrofit): RiderHistoryService {
        return retrofit.create(RiderHistoryService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideProfileRepository(riderHistoryService: RiderHistoryService,appSharedPref: AppSharedPref): RiderHistoryRepository {
        return RiderHistoryRepository(riderHistoryService,appSharedPref)
    }

}
