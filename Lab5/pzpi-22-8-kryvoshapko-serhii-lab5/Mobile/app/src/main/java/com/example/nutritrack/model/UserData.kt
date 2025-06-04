package com.example.nutritrack.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UserData(
    val user_uid: String = "",
    val nickname: String = "",
    val profile_picture: String = "",
    val profile_description: String = "",
    val gender: String = "",
    val height: Int = 0,
    val current_weight: Int = 0,
    val created_at: String = "",
    val last_login: String = "",
    val is_active: Boolean = false,
    val activity_level: Int = 0,
    val birth_year: Int = 0,
    val weightMeasurements: List<String> = emptyList(),
    val userGoals: List<String> = emptyList(),
    val mealEntries: List<String> = emptyList(),
    val exerciseEntries: List<String> = emptyList(),
    val waterIntakes: List<String> = emptyList(),
    val streakHistories: List<String> = emptyList(),
    val userConsultants: List<String> = emptyList(),
    val consultantNotes: List<String> = emptyList(),
    val consultantRequests: List<String> = emptyList()
)
