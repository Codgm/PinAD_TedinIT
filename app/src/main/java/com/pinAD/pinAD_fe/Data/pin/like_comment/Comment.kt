package com.pinAD.pinAD_fe.Data.pin.like_comment

data class Comment(
    val id: Int,
    val author__username: String,
    val content: String,
    val created_at: String,
)
