package com.myoptimind.getexpress.di

import com.myoptimind.getexpress.features.CUSTOMER.home.HomeRepository
import com.myoptimind.getexpress.features.CUSTOMER.home.api.HomeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class HomeModule {

    @ActivityRetainedScoped
    @Provides
    fun provideHomeService(retrofit: Retrofit): HomeService {
        return retrofit.create(HomeService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideHomeRepository(homeService: HomeService): HomeRepository {
        return HomeRepository(homeService)
    }

}
