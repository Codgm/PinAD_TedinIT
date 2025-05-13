package com.pinAD.pinAD_fe.Data.login_register

data class RegisteredAccount(
    val email: String,
    val password: String,
    val fcm_token: String
)
