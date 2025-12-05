package com.xtiantech.mzk.data

// 活动报名请求参数
data class RegisterEventRequest(
    val event_name: String,    // 活动名称
    val game_username: String, // 游戏用户名
    val team_name: String,     // 车队名称
    val group_type: String,    // 参与组别（专业组/业余组/新手组）
    val supplement: String = ""// 补充说明（选填）
)