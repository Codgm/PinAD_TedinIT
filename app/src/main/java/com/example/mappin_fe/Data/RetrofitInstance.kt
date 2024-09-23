package com.example.mappin_fe.Data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://bd9d-175-198-127-14.ngrok-free.app"

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(List::class.java, TagsDeserializer())
        .create()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Token 381e06cef85ad6823f1a6a589666f2d62f48b6b6")
                    .build()
                chain.proceed(requestWithToken)
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
