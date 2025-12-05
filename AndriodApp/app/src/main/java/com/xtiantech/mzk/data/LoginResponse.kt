package com.xtiantech.mzk.data

// 登录响应数据
data class LoginResponse(
    val access_token: String, // 登录令牌
    val user: UserInfo        // 用户基本信息
)

// 嵌套的用户信息模型
data class UserInfo(
    val id: Int,
    val username: String,
    val email: String? = null // 可选字段，允许为空
)