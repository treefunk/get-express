package com.myoptimind.getexpress.di

import com.myoptimind.getexpress.features.customer.home.vehicle_options.VehicleOptionsRepository
import com.myoptimind.getexpress.features.customer.home.vehicle_options.api.VehicleOptionsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class VehicleOptionsModule {

    @ActivityRetainedScoped
    @Provides
    fun provideVehicleOptionsService(retrofit: Retrofit): VehicleOptionsService {
        return retrofit.create(VehicleOptionsService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideVehicleOptionsRepository(customerRequestService: VehicleOptionsService): VehicleOptionsRepository {
        return VehicleOptionsRepository(customerRequestService)
    }
}