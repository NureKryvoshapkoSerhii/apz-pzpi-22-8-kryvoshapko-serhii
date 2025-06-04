package com.example.nutritrack.data.user

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutritrack.model.UserRegistrationData
import javax.inject.Inject

class UserRegistrationViewModel @Inject constructor() : ViewModel() {
    private val _userData = mutableStateOf(UserRegistrationData())
    val userData = _userData

    fun setIdToken(idToken: String) {
        _userData.value = _userData.value.copy(idToken = idToken)
    }

    fun setNickname(nickname: String) {
        _userData.value = _userData.value.copy(nickname = nickname)
    }

    fun setProfilePicture(profilePicture: String) {
        _userData.value = _userData.value.copy(profile_picture = profilePicture)
    }

    fun setProfileDescription(description: String) {
        _userData.value = _userData.value.copy(profile_description = description)
    }

    fun setGender(gender: String) {
        _userData.value = _userData.value.copy(gender = gender)
    }

    fun setHeight(height: Int) {
        _userData.value = _userData.value.copy(height = height)
    }

    fun setCurrentWeight(currentWeight: Int) {
        _userData.value = _userData.value.copy(current_weight = currentWeight)
    }

    fun setActivityLevel(activityLevel: Int) {
        _userData.value = _userData.value.copy(activity_level = activityLevel)
    }

    fun setBirthYear(birthYear: Int) {
        _userData.value = _userData.value.copy(birth_year = birthYear)
    }

    fun clearData() {
        _userData.value = UserRegistrationData()
    }
}