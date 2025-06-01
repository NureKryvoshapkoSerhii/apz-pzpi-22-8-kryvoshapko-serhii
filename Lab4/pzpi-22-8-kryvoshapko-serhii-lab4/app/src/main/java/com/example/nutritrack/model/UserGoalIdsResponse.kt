package com.example.nutritrack.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GoalIdResponse(
    @SerializedName("goal_id") val goalId: Int
)

data class GoalResponse(
    @SerializedName("goal_id") val goalId: Int,
    @SerializedName("user_uid") val userUid: String,
    @SerializedName("consultant_uid") val consultantUid: String?,
    @SerializedName("goal_type") val goalType: Int,
    @SerializedName("target_weight") val targetWeight: Int,
    @SerializedName("duration_weeks") val durationWeeks: Int,
    @SerializedName("daily_calories") val dailyCalories: Double,
    @SerializedName("daily_protein") val dailyProtein: Double,
    @SerializedName("daily_carbs") val dailyCarbs: Double,
    @SerializedName("daily_fats") val dailyFats: Double,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("is_approved_by_consultant") val isApprovedByConsultant: Boolean,
    @SerializedName("user") val user: User?,
    @SerializedName("consultant") val consultant: ConsultantUserGoal?,
    @SerializedName("warning") val warning: String?
)

data class User(
    @SerializedName("user_uid") val userUid: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profile_picture") val profilePicture: String
)

data class ConsultantUserGoal(
    @SerializedName("consultant_uid") val consultantUid: String? = null
)