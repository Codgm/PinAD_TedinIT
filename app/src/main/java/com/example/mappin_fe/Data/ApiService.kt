package com.example.mappin_fe.Data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // 예: 사용자 등록 API
    @POST("register")
    fun registerUser(@Body userAccount: UserAccount): Call<Void>

    // 예: 사용자 로그인 API
    @POST("login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<UserAccount>

    // 예: 사용자 정보 가져오기 API
    @GET("user/{id}")
    fun getUser(@Path("id") userId: String): Call<UserAccount>

    @POST("user/settings")  // 백엔드 서버의 엔드포인트에 맞게 수정
    fun saveUserSettings(@Body userAccount: UserAccount): Call<Void>

    @GET("pins/") // 핀 데이터 API 엔드포인트
    suspend fun getUserPins(): List<PinDataResponse>

    @Multipart
    @POST("pins/")
    suspend fun savePinDataWithMedia(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("range") range: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("mainCategory") mainCategory: RequestBody,
        @Part("subCategory") subCategory: RequestBody,
        @Part mediaFiles: List<MultipartBody.Part>,
        @Part("info") info: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part("visibility") visibility: RequestBody,
    ): Response<PinDataResponse>

}