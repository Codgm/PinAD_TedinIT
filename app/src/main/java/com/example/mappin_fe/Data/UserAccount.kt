package com.example.mappin_fe.Data

data class UserAccount(
    val nickname: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val profilePhoto: String? = null,
    val tags: List<String>? = null,
    val shopping_area: List<String>? = null,
    val brand_preference: List<String>? = null,
    val priority: List<String>? = null,
    val hobby: List<String>? = null,
    val shopping_time: String? = null,
    val notification_radius: Int? = null,
    val notification_number: Int? = null,
//    val couponGift: String? = null,
//    val productImage: String? = null,
//    val relatedImage: String? = null
)