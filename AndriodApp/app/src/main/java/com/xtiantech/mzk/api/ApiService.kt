package com.xtiantech.mzk.api

import com.xtiantech.mzk.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// 新增：注册请求体（和后端参数对应）
data class RegisterRequest(
    val username: String,
    val email: String,
    val phone: String,
    val password: String
)

// 新增：注册响应体
data class RegisterResponse(
    val message: String,
    val user_id: Int
)

// Retrofit接口定义（所有后端接口）
interface ApiService {
    // 新增：注册接口
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    // 登录接口（POST请求，传JSON请求体）
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // 获取用户个人信息
    @GET("api/user/me")
    suspend fun getUserProfile(): UserProfileResponse

    // 活动报名接口
    @POST("api/events/register")
    suspend fun registerEvent(@Body request: RegisterEventRequest): RegisterEventResponse

    // 查看我的报名记录
    @GET("api/events/my")
    suspend fun getMyRegistrations(): MyRegistrationsResponse
}