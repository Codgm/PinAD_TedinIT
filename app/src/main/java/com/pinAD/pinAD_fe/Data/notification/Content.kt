package com.pinAD.pinAD_fe.Data.notification

data class Content(
    val expires_at: String,
    val request_id: Int,
    val valid_until: String,
    val product_name: String,
    val business_name: String,
    val discount_type: String,
    val action_required: Boolean,
    val approvals_count: Int,
    val discount_amount: String
)
