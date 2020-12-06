package com.myoptimind.getexpress.di

import com.myoptimind.getexpress.features.edit_profile.ProfileRepository
import com.myoptimind.getexpress.features.edit_profile.api.ProfileService
import com.myoptimind.getexpress.features.shared.AppSharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class ProfileModule {

    @ActivityRetainedScoped
    @Provides
    fun provideProfileService(retrofit: Retrofit): ProfileService {
        return retrofit.create(ProfileService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideProfileRepository(profileService: ProfileService,appSharedPref: AppSharedPref): ProfileRepository {
        return ProfileRepository(profileService,appSharedPref)
    }

}
