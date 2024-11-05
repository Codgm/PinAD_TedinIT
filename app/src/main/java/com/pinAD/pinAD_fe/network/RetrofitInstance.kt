package com.pinAD.pinAD_fe.network

import com.google.gson.GsonBuilder
import com.pinAD.pinAD_fe.Data.pin.CustomDateTypeAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

object RetrofitInstance {
    const val BASE_URL = "https://f5e9-175-198-127-14.ngrok-free.app/"
    private var accessToken: String? = null
    private var refreshToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청과 응답의 바디를 모두 로깅
    }

    fun setTokens(access: String?, refresh: String?) {
        accessToken = access
        refreshToken = refresh
        if (accessToken?.isNotEmpty() == true && refreshToken?.isNotEmpty() == true) {
            android.util.Log.d("RetrofitInstance", "Tokens updated - Access: $access, Refresh: $refresh")
        } else {
            android.util.Log.d("RetrofitInstance", "AccessToken cleared")
        }
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, CustomDateTypeAdapter())
        .setLenient()
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

    fun getAccessToken(): String? {
        return accessToken
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

    private fun refreshAccessToken(refreshToken: String): Pair<String, String>? {
        // 여기에 실제 토큰 갱신 로직 구현
        // 서버에 refresh_token을 보내고 새로운 access_token과 refresh_token을 받아옴
        // 성공하면 Pair(newAccessToken, newRefreshToken)을 반환, 실패하면 null 반환
        return null // 임시 반환값, 실제 구현 필요
    }

}
