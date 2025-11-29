package com.xtiantech.mzk.data

data class RegisterEventRequest(
    val event_name: String,
    val game_username: String,
    val team_name: String,
    val group_type: String,
    val supplement: String = ""
)