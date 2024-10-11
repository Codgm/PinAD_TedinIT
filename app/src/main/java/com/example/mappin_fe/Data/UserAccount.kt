package com.example.mappin_fe.Data

data class UserAccount(
    val nickname: String? = null,
    val gender: String? = null,
    val age: String? = null,
    val profilePhoto: String? = null,
    val shoppingInterests: List<String>? = null,
    val shoppingAreas: List<String>? = null,
    val brandPreferences: List<String>? = null,
    val shoppingPriorities: List<String>? = null,
    val hobbiesInterests: List<String>? = null,
    val preferredShoppingTime: String? = null,
    val notificationRadius: String? = null,
    val maxPushNotifications: String? = null,
    val couponGift: String? = null,
    val productImage: String? = null,
    val relatedImage: String? = null
)