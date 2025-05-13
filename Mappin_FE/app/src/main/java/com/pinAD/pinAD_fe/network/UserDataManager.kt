package com.pinAD.pinAD_fe.network

import com.pinAD.pinAD_fe.Data.user_data.ProfileData

object UserDataManager {
    public var userData: ProfileData? = null
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION = 5 * 60 * 1000 // 5분 캐시

    suspend fun getUserData(forceRefresh: Boolean = false): ProfileData? {
        // 캐시된 데이터가 있고, 강제 새로고침이 아니며, 캐시 시간이 지나지 않았다면 캐시된 데이터 반환
        if (!forceRefresh && userData != null && (System.currentTimeMillis() - lastFetchTime) < CACHE_DURATION) {
            return userData
        }

        return try {
            // API 호출하여 사용자 데이터 가져오기
            val response = RetrofitInstance.api.getUserProfile()
            if (response.isSuccessful) {
                userData = response.body()
                lastFetchTime = System.currentTimeMillis()
                userData
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // 데이터 수동 업데이트 (포인트 충전 등 데이터 변경 시 호출)
    fun updateUserData(newData: ProfileData) {
        userData = newData
        lastFetchTime = System.currentTimeMillis()
    }

    // 캐시 클리어
    fun clearCache() {
        userData = null
        lastFetchTime = 0
    }
}