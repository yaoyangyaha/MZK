package com.xtiantech.mzk.data

// 用户个人信息响应
data class UserProfileResponse(
    val id: Int,
    val username: String,
    val email: String?,
    val phone: String?,
    val created_at: String
)