package com.example.data.api.network

import android.content.Context
import com.example.data.api.config.ApiConfig
import com.example.data.api.security.SecureIdentityManager
import com.example.data.api.security.SecureTokenManager
import com.example.data.api.service.DeveloperApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central ApiClient factory configuring Retrofit, OkHttpClient, timeouts,
 * JSON converters, and security interceptors for the Developer API layer.
 */
class ApiClient private constructor(context: Context) {

    private val identityManager = SecureIdentityManager.getInstance(context)
    private val tokenManager = SecureTokenManager.getInstance(context)

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(NetworkErrorInterceptor(context))
        .addInterceptor(AuthInterceptor(tokenManager, identityManager))
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: DeveloperApiService by lazy {
        retrofit.create(DeveloperApiService::class.java)
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
