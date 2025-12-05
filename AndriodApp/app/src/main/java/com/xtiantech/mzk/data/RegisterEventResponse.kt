package com.xtiantech.mzk.data

// 报名响应数据
data class RegisterEventResponse(
    val message: String,       // 报名结果提示
    val registration: Registration // 报名记录
)