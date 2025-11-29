package com.xtiantech.mzk.api

import com.xtiantech.mzk.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // 登录
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // 获取用户信息
    @GET("api/user/me")
    suspend fun getUserProfile(): UserProfileResponse

    // 活动报名
    @POST("api/events/register")
    suspend fun registerEvent(@Body request: RegisterEventRequest): RegisterEventResponse

    // 查看我的报名
    @GET("api/events/my")
    suspend fun getMyRegistrations(): MyRegistrationsResponse
}