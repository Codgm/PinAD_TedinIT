package com.example.mappin_fe.Data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserAccount(
    val idToken: String? = null,
    val emailId: String? = null,
    val password: String? = null,
    val phoneNumber: String? = null,
    val nickname: String? = null,
    val interests: String? = null,
    val profilePicUrl: String? = null
)

