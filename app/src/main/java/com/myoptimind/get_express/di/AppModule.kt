package com.myoptimind.get_express.di

import android.content.Context
import com.myoptimind.get_express.BuildConfig
import com.myoptimind.get_express.features.shared.AppSharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
            val okHttpClient =  OkHttpClient.Builder().addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder().addHeader(
                        "Authorization", "Basic ${BuildConfig.AUTHORIZATIONKEY}"
                    )
                        .build())
            }

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(loggingInterceptor)
        okHttpClient.apply {

            addInterceptor(loggingInterceptor)
        }

        return Retrofit.Builder()
            .baseUrl("http://getapp.betaprojex.com/api/")
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAppSharedPref(
        @ApplicationContext context: Context
    ): AppSharedPref {
        return AppSharedPref(context)
    }
}