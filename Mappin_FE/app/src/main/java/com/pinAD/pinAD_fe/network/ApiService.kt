package com.pinAD.pinAD_fe.network

import com.google.gson.JsonObject
import com.pinAD.pinAD_fe.Data.business.BusinessCreateRequest
import com.pinAD.pinAD_fe.Data.notification.Notification
import com.pinAD.pinAD_fe.Data.coupon.CouponResponse
import com.pinAD.pinAD_fe.Data.coupon.CouponStatusResponse
import com.pinAD.pinAD_fe.Data.coupon.CouponVerifyRequest
import com.pinAD.pinAD_fe.Data.coupon.RequestedCouponResponse
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
import com.pinAD.pinAD_fe.Data.login_register.VerificationResponse
import com.pinAD.pinAD_fe.Data.notification.BaseNotification
import com.pinAD.pinAD_fe.Data.notification.BusinessNotification
import com.pinAD.pinAD_fe.Data.notification.CouponApprovalResponse
import com.pinAD.pinAD_fe.Data.notification.NotificationResBusiness
import com.pinAD.pinAD_fe.Data.notification.NotificationResponse
import com.pinAD.pinAD_fe.Data.pin.PaginatedResponse
import com.pinAD.pinAD_fe.Data.pin_review.ReviewRequest
import com.pinAD.pinAD_fe.Data.pin.TagSearchResponse
import com.pinAD.pinAD_fe.Data.pin.like_comment.Comment
import com.pinAD.pinAD_fe.Data.pin.like_comment.LikeResponse
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

    @GET("users/verify-email/{token}/")
    fun verifyEmailToken(@Path("token") token: String): Call<VerificationResponse>

    @POST("users/login/")
    suspend fun loginown(@Body RegisteredAccount: RegisteredAccount): Response<LoginResponse>

    @Multipart
    @PUT("users/")
    suspend fun updateUserSettings(
        @Header("Authorization") token: String,
        @Part profile_picture: MultipartBody.Part?,
        @Part ("user_data") userAccount: RequestBody
    ): Response<Void>

    @Multipart
    @PUT("users/")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Part("user_data") userData: RequestBody,
        @Part profile_picture: MultipartBody.Part?
    ): Response<ProfileData>

    @Multipart
    @PUT("users/")
    suspend fun updateRadius (
        @Header("Authorization") token: String,
        @Part("user_data") userData: RequestBody,
    ): Response<ProfileData>

    @GET("users/")
    suspend fun getUserProfile(
    ): Response<ProfileData>

    @GET("users/")
    suspend fun checkUserProfile(
        @Header("Authorization") token: String,
    ): Response<JsonObject>

    @POST("users/update-location/")
    suspend fun updateUserLocation(
        @Header("Authorization") token: String,
        @Body location: LocationUpdateRequest
    ): PinNotificationResponse

    @GET("pins/") // 핀 데이터 API 엔드포인트
    suspend fun getPins(
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("radius") radius: Int?,
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
    @POST("users/coupon/")
    suspend fun saveCouponPinWithMedia(
        @Part("product_name") title: RequestBody,
        @Part("discount_amount") discount_amount: RequestBody,
        @Part("discount_type") discount_type: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("max_num") max_num : RequestBody,
//        @Part("range") range: RequestBody,
//        @Part("duration") duration: RequestBody,
//        @Part("pin_type") pin_type: RequestBody,
//        @Part media_files: List<MultipartBody.Part>,
//        @Part("tag_ids") tag_ids: ArrayList<RequestBody>,
//        @Part("visibility") visibility: RequestBody,
//        @Part("is_ads") is_ads: RequestBody,
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
        @Query("radius") radius: Int,
        @Query("page_size") page_size: Int
    ): Response<PaginatedResponse<FltPinData>>

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

    @GET("/users/get_user_coupon_request/")
    suspend fun showRequestedCoupons(
        @Header("Authorization") token: String
    ): Response<List<RequestedCouponResponse>>

    @POST("coupons/verify/")
    suspend fun verifyCoupon(
        @Body request: CouponVerifyRequest
    ): Response<Void>

    @POST("pins/like/{pinId}/")
    fun likePin(@Path("pinId") pinId: Int): Call<LikeResponse>

    @GET("pins/get_comments/{pinId}/")
    fun getComments(@Header("Authorization") token: String, @Path("pinId") pinId: Int): Call<List<Comment>>

    @POST("pins/add_comment/{pinId}/")
    fun addComment(@Path("pinId") pinId: Int, @Body content: Map<String, String>): Call<Comment>

    @DELETE("pins/delete_comments/{commentId}/")
    fun deleteComment(@Path("commentId")  commentId: Int): Call<ResponseBody>

    @GET("users/get_notification/")
    suspend fun getUserNotifications(): Response<List<BaseNotification>>

    @GET("users/get_business_notification/")
    suspend fun getBusinessUserNotifications(): Response<List<BaseNotification>>

    @POST("users/coupon/approve/")
    suspend fun approveCouponRequest(
        @Body notificationResBusiness: NotificationResBusiness
    ): Response<CouponApprovalResponse>

    @POST("users/coupon/response/")
    suspend fun respondToCoupon(
        @Body notificationResponse: NotificationResponse
    ): Response<ResponseBody>

    @GET("coupons/get_coupon_status/")
    suspend fun getCouponStatus(
        @Query("coupon_id") couponId: String
    ): Response<CouponStatusResponse>

    @POST("purchases/verify")
    suspend fun verifyPurchase(
        @Header("Authorization") token: String,
        @Body purchaseInfo: PurchaseInfo
    ): Response<Any>

    @GET("users/check_business/")
    suspend fun checkBusiness() : Response<Boolean>

    @POST("users/business/")
    suspend fun createBusiness(@Body business: BusinessCreateRequest): Response<Void>

}