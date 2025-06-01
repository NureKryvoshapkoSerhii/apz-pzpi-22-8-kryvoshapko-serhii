package com.example.nutritrack.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.nutritrack.data.consultant.ConsultantRegistrationViewModel
import com.example.nutritrack.screens.consultant.ConsultantSuccessScreen
import com.example.nutritrack.screens.registration.consultant.ConsultantExperienceYearsScreen
import com.example.nutritrack.screens.registration.consultant.ConsultantGenderSelectionScreen
import com.example.nutritrack.screens.registration.consultant.ConsultantMaxClientsScreen
import com.example.nutritrack.screens.registration.consultant.ConsultantNicknameScreen
import com.example.nutritrack.screens.registration.consultant.ConsultantProfileDescriptionScreen


fun NavGraphBuilder.consultantRegistrationNavGraph(
    navController: NavHostController,
    viewModel: ConsultantRegistrationViewModel
) {
    navigation(
        startDestination = "consultant_gender_selection_screen",
        route = "consultant_registration_graph"
    ) {

        composable("consultant_gender_selection_screen") {
            ConsultantGenderSelectionScreen(
                viewModel = viewModel,
                onNextClick = {
                    navController.navigate("consultant_nickname_screen")
                }
            )
        }
        composable("consultant_nickname_screen") {
            ConsultantNicknameScreen(
                viewModel = viewModel,
                onNextClick = {
                    navController.navigate("consultant_profile_description_screen")
                }
            )
        }
        composable("consultant_profile_description_screen") {
            ConsultantProfileDescriptionScreen(
                viewModel = viewModel,
                onNextClick = {
                    navController.navigate("consultant_experience_years_screen")
                }
            )
        }
        composable("consultant_experience_years_screen") {
            ConsultantExperienceYearsScreen(
                viewModel = viewModel,
                onNextClick = {
                    navController.navigate("consultant_max_clients_screen")
                }
            )
        }
        composable("consultant_max_clients_screen") {
            ConsultantMaxClientsScreen(
                viewModel = viewModel,
                onRegistrationSuccess = {
                    navController.navigate("consultant_success_screen")
                },
                navController = navController
            )
        }
        composable("consultant_success_screen") {
            ConsultantSuccessScreen(
                onNavigateToMainScreen = {
                    navController.navigate("consultant_main_graph") {
                        popUpTo("consultant_registration_graph") { inclusive = true }
                    }
                }
            )
        }
    }
}