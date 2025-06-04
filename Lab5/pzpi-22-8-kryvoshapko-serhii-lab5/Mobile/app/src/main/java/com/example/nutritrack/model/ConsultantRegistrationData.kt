package com.example.nutritrack.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ConsultantRegistrationData(
    val idToken: String = "",
    val nickname: String = "",
    val profile_picture: String = "",
    val profile_description: String = "",
    val experience_years: Int = 0,
    val max_clients: Int = 0,
    val gender: String = ""
)