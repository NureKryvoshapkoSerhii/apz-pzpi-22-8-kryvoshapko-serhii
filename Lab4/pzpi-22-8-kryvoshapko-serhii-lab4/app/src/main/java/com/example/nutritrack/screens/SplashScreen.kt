package com.example.nutritrack.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "App Icon",
            modifier = Modifier.size(170.dp)
        )
    }

    LaunchedEffect(Unit) {
        val isAuthenticated = FirebaseAuthHelper.isUserAuthenticated()
        if (!isAuthenticated) {
            Log.d("SplashScreen", "User is not authenticated")
            navController.navigate("welcome_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val idToken = FirebaseAuthHelper.getIdToken()
        val uid = FirebaseAuthHelper.getUid()
        if (idToken == null || uid == null) {
            Log.d("SplashScreen", "Failed to get idToken or uid")
            navController.navigate("welcome_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val isConsultant = withContext(Dispatchers.IO) {
            ApiService.checkConsultantExists(idToken)
        }
        Log.d("SplashScreen", "Is Consultant: $isConsultant")

        if (isConsultant) {
            Log.d("SplashScreen", "Consultant authenticated, navigating to ConsultantSuccessScreen")
            navController.navigate("consultant_main_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val userData = withContext(Dispatchers.IO) {
            ApiService.getUserByUid(uid)
        }
        Log.d("SplashScreen", "User Data: $userData")

        if (userData == null || !hasRequiredUserData(userData)) {
            Log.d("SplashScreen", "User data missing or incomplete")
            navController.navigate("welcome_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val goalIds = withContext(Dispatchers.IO) {
            ApiService.getAllUserGoalIds(idToken)
        }
        Log.d("SplashScreen", "Goal IDs: $goalIds")

        if (goalIds.isNotEmpty()) {
            Log.d("SplashScreen", "Goals exist, navigating to UserMainScreen")
            navController.navigate("user_main_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        } else {
            Log.d("SplashScreen", "No goals, navigating to UserSuccessScreen")
            navController.navigate("user_success_screen") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }
}

private fun hasRequiredUserData(userData: UserData): Boolean {
    val hasData = userData.gender.isNotEmpty() &&
            userData.height > 0 &&
            userData.current_weight > 0
    Log.d("SplashScreen", "Has required user data: $hasData")
    return hasData
}