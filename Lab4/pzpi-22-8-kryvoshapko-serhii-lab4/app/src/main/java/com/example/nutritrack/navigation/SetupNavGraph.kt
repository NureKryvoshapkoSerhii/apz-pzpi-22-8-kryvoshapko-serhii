package com.example.nutritrack.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nutritrack.data.consultant.ConsultantRegistrationViewModel
import com.example.nutritrack.data.user.UserGoalViewModel
import com.example.nutritrack.data.user.UserRegistrationViewModel
import com.example.nutritrack.screens.SplashScreen
import com.example.nutritrack.screens.WelcomeScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupNavGraph(navController: NavHostController) {
    val consultantViewModel: ConsultantRegistrationViewModel = viewModel()
    val userViewModel: UserRegistrationViewModel = viewModel()
    val userGoalViewModel: UserGoalViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = "splash_screen"
    ) {
        composable("splash_screen") {
            SplashScreen(navController = navController)
        }
        composable("welcome_screen") {
            WelcomeScreen(
                onNewUserClick = {
                    navController.navigate("user_registration_graph")
                },
                onConsultantClick = {
                    navController.navigate("consultant_registration_graph")
                },
                onLoginAsUserClick = {
                    navController.navigate("user_main_graph") {
                        popUpTo("welcome_screen") { inclusive = true }
                    }
                },
                onLoginAsConsultantClick = { navController.navigate("consultant_main_screen") },
            )
        }
        userRegistrationNavGraph(navController, userViewModel, userGoalViewModel)
        userMainNavGraph(navController)
        consultantRegistrationNavGraph(navController, consultantViewModel)
        consultantMainNavGraph(navController)
    }
}