package com.example.nutritrack.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UserRegistrationData(
    val idToken: String = "",
    val nickname: String = "",
    val profile_picture: String = "",
    val profile_description: String = "",
    val gender: String = "",
    val height: Int = 0,
    val current_weight: Int = 0,
    val activity_level: Int = 0, //Enum value (0-Sedentary, 1-Light, 2-Moderate, 3-High)
    val birth_year: Int = 0
)
