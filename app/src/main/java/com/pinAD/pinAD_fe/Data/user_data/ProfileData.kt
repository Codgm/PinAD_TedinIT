package com.pinAD.pinAD_fe.Data.user_data
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileData(
    val gender: String?,
    val nickname: String?,
    val tags: List<String>?,
    val points: Int?,
    val profile_picture: String?
) : Parcelable
