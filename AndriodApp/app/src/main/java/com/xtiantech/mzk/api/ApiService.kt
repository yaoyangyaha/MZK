package com.xtiantech.mzk.api

import com.xtiantech.mzk.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Retrofit接口定义（所有后端接口）
interface ApiService {
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