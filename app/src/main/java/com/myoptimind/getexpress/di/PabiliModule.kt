package com.myoptimind.getexpress.di



import com.myoptimind.getexpress.features.customer.pabili.PabiliRepository
import com.myoptimind.getexpress.features.customer.pabili.api.PabiliService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class PabiliModule {

    @ActivityRetainedScoped
    @Provides
    fun provideDeliveryService(retrofit: Retrofit): PabiliService {
        return retrofit.create(PabiliService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideDeliveryRepository(pabiliService: PabiliService): PabiliRepository {
        return PabiliRepository(pabiliService)
    }
}