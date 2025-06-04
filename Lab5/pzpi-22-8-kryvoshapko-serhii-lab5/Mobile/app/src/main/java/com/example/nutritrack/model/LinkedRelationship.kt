package com.example.nutritrack.model

import kotlinx.serialization.Serializable

@Serializable
data class LinkedRelationship(
    val linkId: Int,
    val userUid: String,
    val consultantUid: String,
    val assignmentDate: String,
    val isActive: Boolean,
    val user: LinkedUser,
    val consultant: LinkedConsultant,
)

@Serializable
data class LinkedUser(
    val user_uid: String,
    val nickname: String,
    val profile_picture: String,
    val gender: String
)

@Serializable
data class LinkedConsultant(
    val consultant_uid: String,
    val nickname: String,
    val profile_picture: String,
    val profile_description: String,
    val experience_years: Int
)

@Serializable
data class RemoveConsultantRequest(
    val idToken: String,
    val consultant_uid: String
)