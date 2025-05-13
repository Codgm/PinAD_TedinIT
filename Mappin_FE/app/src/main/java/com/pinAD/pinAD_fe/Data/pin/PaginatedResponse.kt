package com.pinAD.pinAD_fe.Data.pin

data class PaginatedResponse<FltPinData>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<FltPinData>
)
