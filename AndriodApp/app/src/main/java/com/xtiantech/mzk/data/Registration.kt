package com.xtiantech.mzk.data

data class Registration(
    val id: Int,
    val event_name: String,
    val game_username: String,
    val team_name: String,
    val group_type: String,
    val supplement: String,
    val user_id: Int,
    val username: String,
    val registered_at: String
)

data class MyRegistrationsResponse(
    val total: Int,
    val registrations: List<Registration>
)