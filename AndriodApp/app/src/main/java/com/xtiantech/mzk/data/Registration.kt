package com.xtiantech.mzk.data

// 报名记录模型（单条）
data class Registration(
    val id: Int,
    val event_name: String,
    val game_username: String,
    val team_name: String,
    val group_type: String,
    val supplement: String,
    val user_id: Int,
    val username: String,
    val registered_at: String // 报名时间（格式：yyyy-MM-dd HH:mm:ss）
)

// 我的报名列表响应
data class MyRegistrationsResponse(
    val total: Int,                // 报名总数
    val registrations: List<Registration> // 报名列表
)