package com.example.nutritrack.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateConsultantNicknameRequest(
    val idToken: String,
    val new_nickname: String
)

@Serializable
data class UpdateConsultantProfilePictureRequest(
    val idToken: String,
    val new_profile_picture: String
)

@Serializable
data class UpdateConsultantProfileDescriptionRequest(
    val idToken: String,
    val new_profile_description: String
)

@Serializable
data class UpdateConsultantMaxClientsRequest(
    val idToken: String,
    val new_max_clients: Int
)

@Serializable
data class UpdateConsultantExperienceYearsRequest(
    val idToken: String,
    val new_experience_years: Int
)