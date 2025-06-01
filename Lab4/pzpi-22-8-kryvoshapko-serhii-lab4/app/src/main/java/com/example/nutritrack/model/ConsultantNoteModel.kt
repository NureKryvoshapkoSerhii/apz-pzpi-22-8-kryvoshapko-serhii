package com.example.nutritrack.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AddNoteRequest(
    val idToken: String,
    val goal_id: Int,
    val content: String
)

@Serializable
data class UpdateNoteRequest(
    val idToken: String,
    val note_id: Int,
    val content: String
)