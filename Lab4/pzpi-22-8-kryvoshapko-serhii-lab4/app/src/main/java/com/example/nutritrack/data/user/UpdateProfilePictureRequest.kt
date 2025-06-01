package com.example.nutritrack.data.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserProfilePictureRequest(
    val idToken: String,
    val new_profile_picture: String
)

@Serializable
data class UpdateUserNicknameRequest(
    val idToken: String,
    val new_nickname: String
)

@Serializable
data class UpdateUserProfileDescriptionRequest(
    val idToken: String,
    val new_profile_description: String
)

@Serializable
data class UpdateUserCurrentWeightRequest(
    val idToken: String,
    val new_current_weight: Int
)
