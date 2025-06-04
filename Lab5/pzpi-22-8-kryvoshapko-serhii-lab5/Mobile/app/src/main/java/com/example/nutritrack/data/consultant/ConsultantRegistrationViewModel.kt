package com.example.nutritrack.data.consultant

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutritrack.model.ConsultantRegistrationData
import javax.inject.Inject

class ConsultantRegistrationViewModel @Inject constructor() : ViewModel() {
    private val _consultantData = mutableStateOf(ConsultantRegistrationData())
    val consultantData = _consultantData

    fun setIdToken(idToken: String) {
        _consultantData.value = _consultantData.value.copy(idToken = idToken)
    }

    fun setNickname(nickname: String) {
        _consultantData.value = _consultantData.value.copy(nickname = nickname)
    }

    fun setProfilePicture(profilePicture: String) {
        _consultantData.value = _consultantData.value.copy(profile_picture = profilePicture)
    }

    fun setProfileDescription(description: String) {
        _consultantData.value = _consultantData.value.copy(profile_description = description)
    }

    fun setExperienceYears(years: Int) {
        _consultantData.value = _consultantData.value.copy(experience_years = years)
    }

    fun setMaxClients(maxClients: Int) {
        _consultantData.value = _consultantData.value.copy(max_clients = maxClients)
    }

    fun setGender(gender: String) {
        _consultantData.value = _consultantData.value.copy(gender = gender)
    }

    fun clearData() {
        _consultantData.value = ConsultantRegistrationData()
    }
}