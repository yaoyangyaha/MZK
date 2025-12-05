package com.xtiantech.mzk.data

// 登录请求参数（和后端接口字段一致）
data class LoginRequest(
    val username: String,
    val password: String
)