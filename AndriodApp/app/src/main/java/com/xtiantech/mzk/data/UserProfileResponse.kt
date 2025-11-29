package com.xtiantech.mzk.data

data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val phone: String,
    val created_at: String
)