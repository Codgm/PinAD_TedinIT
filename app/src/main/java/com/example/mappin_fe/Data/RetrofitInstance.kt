package com.example.mappin_fe.Data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://43.200.81.103:8000"

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(List::class.java, TagsDeserializer())
        .create()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestWithToken = originalRequest.newBuilder()
                    .header("Authorization", "Token 0083ef850ca36cb7dbe9ccf7cfba1c1834bb6906")
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
