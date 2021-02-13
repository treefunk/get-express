
package com.myoptimind.get_express.di



import com.myoptimind.get_express.features.customer.whats_new.WhatsNewRepository
import com.myoptimind.get_express.features.customer.whats_new.api.WhatsNewService
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
class WhatsNewModule {

    @ActivityRetainedScoped
    @Provides
    fun provideWhatsNewService(retrofit: Retrofit): WhatsNewService {
        return retrofit.create(WhatsNewService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideWhatsNewRepository(whatsNewService: WhatsNewService): WhatsNewRepository {
        return WhatsNewRepository(whatsNewService)
    }
}