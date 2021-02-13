package com.myoptimind.get_express.di



import com.myoptimind.get_express.features.rider.topup.RiderTopupRepository
import com.myoptimind.get_express.features.rider.topup.api.RiderTopupService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class TopupModule {

    @ActivityRetainedScoped
    @Provides
    fun provideTopupService(retrofit: Retrofit): RiderTopupService {
        return retrofit.create(RiderTopupService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideRiderTopupRepository(riderTopupService: RiderTopupService): RiderTopupRepository {
        return RiderTopupRepository(riderTopupService)
    }
}