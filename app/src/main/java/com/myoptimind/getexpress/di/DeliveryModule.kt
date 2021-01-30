package com.myoptimind.getexpress.di


import com.myoptimind.getexpress.features.customer.delivery.DeliveryRepository
import com.myoptimind.getexpress.features.customer.delivery.api.DeliveryService
import com.myoptimind.getexpress.features.customer.food_grocery.StoresRepository
import com.myoptimind.getexpress.features.customer.food_grocery.api.StoresService
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