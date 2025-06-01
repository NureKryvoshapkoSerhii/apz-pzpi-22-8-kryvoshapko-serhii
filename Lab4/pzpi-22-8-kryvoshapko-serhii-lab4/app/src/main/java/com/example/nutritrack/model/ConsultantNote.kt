package com.example.nutritrack.model

import kotlinx.serialization.Serializable

@Serializable
data class ConsultantNote(
    val note_id: Int,
    val consultant_nickname: String,
    val consultant_uid: String,
    val goal_id: Int,
    val content: String,
    val created_at: String
)