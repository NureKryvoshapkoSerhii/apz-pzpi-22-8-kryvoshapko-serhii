package com.example.nutritrack.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSendInviteRequest(
    val idToken: String,
    val consultant_uid: String
)

@Serializable
data class ConsultantSendInviteRequest(
    val idToken: String,
    val user_uid: String
)

@Serializable
data class ConsultantRequest(
    val requestId: Int,
    val userUid: String,
    val consultantUid: String,
    val status: String,
    val createdAt: String,
    val respondedAt: String? = null,
    val user: UserData,
    val consultant: Consultant
)

@Serializable
data class UserRespondInviteRequest(
    val idToken: String,
    val consultant_uid: String,
    val is_accepted: Boolean
)

@Serializable
data class ConsultantRespondInviteRequest(
    val idToken: String,
    val user_uid: String,
    val is_accepted: Boolean
)