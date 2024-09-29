package com.example.mappin_fe.Data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://77c6-175-198-127-14.ngrok-free.app"
    private var accessToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청과 응답의 바디를 모두 로깅
    }

    fun setAccessToken(token: String) {
        accessToken = token
        if (token.isNotEmpty()) {
            android.util.Log.d("RetrofitInstance", "AccessToken set: $token")
        } else {
            android.util.Log.d("RetrofitInstance", "AccessToken cleared")
        }
    }

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(List::class.java, TagsDeserializer())
        .create()

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            // 요청 헤더와 바디를 로깅하는 커스텀 Interceptor 추가
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // AccessToken이 있을 경우 헤더 추가
                accessToken?.let {
                    android.util.Log.d("RetrofitInstance", "AccessToken: $it")
                    requestBuilder.header("Authorization", "Bearer $it")
                } ?: run {
                    android.util.Log.d("RetrofitInstance", "No AccessToken available")
                }
                val urls = originalRequest.url
                android.util.Log.d("RetrofitInstance", "Request URLS: $urls")


                // 요청 바디 로그 출력
                originalRequest.body?.let { body ->
                    val buffer = okio.Buffer()
                    body.writeTo(buffer)
                    android.util.Log.d("RetrofitInstance", "Request Body: ${buffer.readUtf8()}")
                } ?: run {
                    android.util.Log.d("RetrofitInstance", "No Request Body")
                }

                chain.proceed(requestBuilder.build())
            }
            // 기존의 HttpLoggingInterceptor 추가
            .addInterceptor(loggingInterceptor)
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
