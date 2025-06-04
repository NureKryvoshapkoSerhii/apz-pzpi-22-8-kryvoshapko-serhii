package com.example.nutritrack.data.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutritrack.model.UserGoalData
import javax.inject.Inject

class UserGoalViewModel @Inject constructor() : ViewModel() {
    private val _userData = mutableStateOf(UserGoalData())
    val userData = _userData

    fun setIdToken(idToken: String) {
        _userData.value = _userData.value.copy(idToken = idToken)
    }

    fun setGoalType(goalType: Int) {
        _userData.value = _userData.value.copy(goal_type = goalType)
    }

    fun setTargetWeight(weight: Int) {
        _userData.value = _userData.value.copy(target_weight = weight)
    }

    fun setDurationWeeks(weeks: Int) {
        _userData.value = _userData.value.copy(duration_weeks = weeks)
    }

    fun clearData() {
        _userData.value = UserGoalData()
    }
}