package com.pinAD.pinAD_fe.network

import com.pinAD.pinAD_fe.Data.coupon.CouponResponse
import com.pinAD.pinAD_fe.Data.coupon.CouponVerifyRequest
import com.pinAD.pinAD_fe.Data.pin.FltPinData
import com.pinAD.pinAD_fe.Data.location.LocationUpdateRequest
import com.pinAD.pinAD_fe.Data.login_register.LoginRequest
import com.pinAD.pinAD_fe.Data.login_register.LoginResponse
import com.pinAD.pinAD_fe.Data.pin.PinDataResponse
import com.pinAD.pinAD_fe.Data.pin.PinNotificationResponse
import com.pinAD.pinAD_fe.Data.pin.PinWrapper
import com.pinAD.pinAD_fe.Data.user_data.ProfileData
import com.pinAD.pinAD_fe.Data.purchase.PurchaseInfo
import com.pinAD.pinAD_fe.Data.login_register.RegisterAccount
import com.pinAD.pinAD_fe.Data.login_register.RegisteredAccount
import com.pinAD.pinAD_fe.Data.pin_review.ReviewRequest
import com.pinAD.pinAD_fe.Data.pin.TagSearchResponse
import com.pinAD.pinAD_fe.Data.user_data.UserAccount
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("users/google/login/")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    // 예: 사용자 등록 API
    @POST("users/register/")
    fun registerUser(@Body RegisterAccount: RegisterAccount): Call<Void>

    @POST("users/login/")
    suspend fun loginown(@Body RegisteredAccount: RegisteredAccount): Response<Void>

    @Multipart
    @PUT("users/")
    suspend fun updateUserSettings(
        @Header("Authorization") token: String,
        @Part profile_picture: MultipartBody.Part?,
        @Part ("user_data") userAccount: RequestBody
    ): Response<Void>


    @GET("users/")
    suspend fun getUserProfile(
    ): Response<ProfileData>

    @POST("users/update-location/")
    suspend fun updateUserLocation(
        @Header("Authorization") token: String,
        @Body location: LocationUpdateRequest
    ): PinNotificationResponse

    @GET("pins/") // 핀 데이터 API 엔드포인트
    suspend fun getPins(
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("radius") radius: Int?
    ): Response<List<FltPinData>>

    @GET("pins/user_pins/")
    suspend fun getUserPins(): Response<List<FltPinData>>

    @GET("/pins/get_info/{id}/")
    suspend fun getPinData(@Path("id") id: Int): Response<PinWrapper>

    @Multipart
    @POST("pins/")
    suspend fun savePinDataWithMedia(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("range") range: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("pin_type") pin_type: RequestBody,
//        @Part("category") category: RequestBody,
        @Part media_files: List<MultipartBody.Part>,
        @Part("info") info: RequestBody,
        @Part("tag_ids") tag_ids: ArrayList<RequestBody>,
        @Part("visibility") visibility: RequestBody,
        @Part("is_ads") is_ads: RequestBody,
    ): Response<PinDataResponse>

    @Multipart
    @POST("pins/")
    suspend fun saveReviewDataWithMedia(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("pin_type") pin_type: RequestBody,
//        @Part("category") category: RequestBody,
        @Part media_files: List<MultipartBody.Part>,
        @Part("info") info: RequestBody,
        @Part("tag_ids") tag_ids: ArrayList<RequestBody>,
        @Part("visibility") visibility: RequestBody,
        @Part("is_ads") is_ads: RequestBody,
    ): Response<ReviewRequest>


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
    ): Response<List<CouponResponse>>

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