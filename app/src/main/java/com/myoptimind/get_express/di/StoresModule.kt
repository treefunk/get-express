package com.myoptimind.get_express.di


import com.myoptimind.get_express.features.customer.food_grocery.StoresRepository
import com.myoptimind.get_express.features.customer.food_grocery.api.StoresService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class StoresModule {

    @ActivityRetainedScoped
    @Provides
    fun provideStoresService(retrofit: Retrofit): StoresService {
        return retrofit.create(StoresService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideStoresRepository(storesService: StoresService): StoresRepository {
        return StoresRepository(storesService)
    }
}