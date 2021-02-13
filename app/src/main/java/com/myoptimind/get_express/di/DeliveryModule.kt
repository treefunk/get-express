package com.myoptimind.get_express.di


import com.myoptimind.get_express.features.customer.delivery.DeliveryRepository
import com.myoptimind.get_express.features.customer.delivery.api.DeliveryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class DeliveryModule {

    @ActivityRetainedScoped
    @Provides
    fun provideDeliveryService(retrofit: Retrofit): DeliveryService {
        return retrofit.create(DeliveryService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideDeliveryRepository(deliveryService: DeliveryService): DeliveryRepository {
        return DeliveryRepository(deliveryService)
    }
}