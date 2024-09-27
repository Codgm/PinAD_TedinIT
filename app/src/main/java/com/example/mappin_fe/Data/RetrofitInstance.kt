package com.example.mappin_fe.Data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://2cc3-175-198-127-14.ngrok-free.app/"
    private var accessToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청과 응답의 바디를 모두 로깅
    }


    fun setAccessToken(token: String) {
        accessToken = token
    }

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(List::class.java, TagsDeserializer())
        .create()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                loggingInterceptor

                accessToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }

                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
