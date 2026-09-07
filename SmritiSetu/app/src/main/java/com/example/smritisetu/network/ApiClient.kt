package com.example.smritisetu.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "https://smritisetu-backend-fuab.onrender.com/"

    @Volatile
    var authToken: String? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        authToken?.let { token ->
            if (token.isNotBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val userApi: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val gameApi: GameApiService by lazy { retrofit.create(GameApiService::class.java) }
    val leagueApi: LeagueApiService by lazy { retrofit.create(LeagueApiService::class.java) }
    val shopApi: ShopApiService by lazy { retrofit.create(ShopApiService::class.java) }
    val caregiverApi: CaregiverApiService by lazy { retrofit.create(CaregiverApiService::class.java) }
}
