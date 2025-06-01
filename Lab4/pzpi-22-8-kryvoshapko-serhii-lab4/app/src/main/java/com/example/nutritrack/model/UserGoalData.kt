package com.example.nutritrack.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UserGoalData(
    val idToken: String = "",
    val goal_type: Int = 0, // Enum value (0 - maintain, 1 - decrease, 2 - increase)
    val target_weight: Int = 0,
    val duration_weeks: Int = 0
)

@Keep
@Serializable
data class DeleteGoalRequest(
    val goalId: Int = 0,
    val idToken: String = "",
)