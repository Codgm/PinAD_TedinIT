package com.example.mappin_fe.Data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("users/google/login/")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    // 예: 사용자 등록 API
    @POST("users/register/")
    fun registerUser(@Body RegisterAccount: RegisterAccount): Call<Void>

    @POST("users/login/")
    suspend fun loginown(@Body RegisteredAccount: RegisteredAccount): Response<Void>

    @PUT("users/")
    suspend fun updateUserSettings(
        @Header("Authorization") token: String,
        @Body userAccount: UserAccount
    ): Response<Void>

    @GET("users/")
    suspend fun getUserProfile(
        @Header("Authorization") token: String,
    ): Response<ProfileData>

    @GET("pins/") // 핀 데이터 API 엔드포인트
    suspend fun getUserPins(): Response<List<FltPinData>>

    @GET("pins/")
    suspend fun getPins(): Response<List<FltPinData>>

    @Multipart
    @POST("pins/")
    suspend fun savePinDataWithMedia(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
//        @Part("category") category: RequestBody,
        @Part media_files: List<MultipartBody.Part>,
        @Part("info") info: RequestBody,
        @Part("tag_ids") tag_ids: ArrayList<RequestBody>,
        @Part("visibility") visibility: RequestBody,
        @Part("is_ads") is_ads: RequestBody,
    ): Response<PinDataResponse>

    @POST("api/reviews")
    suspend fun submitReview(@Body reviewRequest: ReviewRequest): Response<ReviewResponse>

    @GET("pins/search/")
    suspend fun searchPins(
        @Query("keyword") keyword: String?,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int
    ): Response<List<FltPinData>>

    @GET("pins/search_tags/")
    suspend fun searchTags(
        @Query("keyword") keyword: String?,
    ): Response<TagSearchResponse>


    @DELETE("users/")
    suspend fun deleteUser(): Response<Unit>

    @POST("/coupons/issue/")
    fun issueCoupon(@Body couponRequest: Map<String, String>): Call<ResponseBody>

    @GET("/coupons/show/")
    suspend fun showCoupons(
        @Header("Authorization") token: String
    ): Response<CouponResponse>

    @POST("coupons/verify/")
    suspend fun verifyCoupon(
        @Body request: CouponVerifyRequest
    ): Response<Void>

    @POST("purchases/verify")
    suspend fun verifyPurchase(
        @Header("Authorization") token: String,
        @Body purchaseInfo: PurchaseInfo
    ): Response<Any>

}