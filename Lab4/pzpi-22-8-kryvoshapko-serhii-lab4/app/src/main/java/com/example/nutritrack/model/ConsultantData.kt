package com.example.nutritrack.model

import kotlinx.serialization.Serializable

@Serializable
data class Consultant(
    val consultant_uid: String,
    val nickname: String,
    val profile_picture: String,
    val profile_description: String,
    val experience_years: Int,
    val is_active: Boolean,
    val created_at: String,
    val last_login: String,
    val max_clients: Int,
    val current_clients: Int,
    val gender: String
)