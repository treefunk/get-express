package com.myoptimind.getexpress.di

import android.content.Context
import com.myoptimind.getexpress.features.CUSTOMER.home.api.HomeService
import com.myoptimind.getexpress.features.login.LoginRepository
import com.myoptimind.getexpress.features.login.api.LoginService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class LoginModule {

    @ActivityRetainedScoped
    @Provides
    fun provideLoginService(retrofit: Retrofit): LoginService {
        return retrofit.create(LoginService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideLoginRepository(loginService: LoginService,
                               homeService: HomeService,
    @ApplicationContext context: Context): LoginRepository {
        return LoginRepository(loginService,homeService,context)
    }
}