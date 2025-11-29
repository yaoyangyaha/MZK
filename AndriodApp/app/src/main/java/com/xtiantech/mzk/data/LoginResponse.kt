package com.xtiantech.mzk.data

data class LoginResponse(
    val access_token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Int,
    val username: String,
    val email: String
)