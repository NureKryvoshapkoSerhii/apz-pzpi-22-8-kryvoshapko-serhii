package com.example.nutritrack.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.nutritrack.data.user.UserGoalViewModel
import com.example.nutritrack.data.user.UserRegistrationViewModel
import com.example.nutritrack.screens.registration.user.ActivityLevelScreen
import com.example.nutritrack.screens.registration.user.CurrentWeightScreen
import com.example.nutritrack.screens.registration.user.GenderSelectionScreen
import com.example.nutritrack.screens.registration.user.HeightSelectionScreen
import com.example.nutritrack.screens.registration.user.UserNicknameScreen
import com.example.nutritrack.screens.registration.user.YearSelectionScreen
import com.example.nutritrack.screens.registration.user.create_goal.UserDesiredWeightScreen
import com.example.nutritrack.screens.registration.user.create_goal.UserGoalTypeScreen
import com.example.nutritrack.screens.registration.user.create_goal.UserWeeksScreen
import com.example.nutritrack.screens.user.UserSuccessScreen

fun NavGraphBuilder.userRegistrationNavGraph(
    navController: NavHostController,
    registrationViewModel: UserRegistrationViewModel,
    goalViewModel: UserGoalViewModel
) {
    navigation(
        startDestination = "gender_selection_screen",
        route = "user_registration_graph"
    ) {
        composable("gender_selection_screen") {
            GenderSelectionScreen(
                viewModel = registrationViewModel,
                onNextClick = {
                    navController.navigate("activity_level_screen")
                }
            )
        }
        composable("activity_level_screen") {
            ActivityLevelScreen(
                viewModel = registrationViewModel,
                onNextClick = {
                    navController.navigate("year_selection_screen")
                }
            )
        }
        composable("year_selection_screen") {
            YearSelectionScreen(
                viewModel = registrationViewModel,
                onNextClick = { navController.navigate("height_selection_screen") }
            )
        }
        composable("height_selection_screen") {
            HeightSelectionScreen(
                viewModel = registrationViewModel,
                onNextClick = {
                    navController.navigate("current_weight_screen")
                }
            )
        }
        composable("current_weight_screen") {
            CurrentWeightScreen(
                viewModel = registrationViewModel,
                onNextClick = { navController.navigate("user_nickname_screen") },
            )
        }
        composable("user_nickname_screen") {
            UserNicknameScreen(
                viewModel = registrationViewModel,
                onRegistrationSuccess = {
                    navController.navigate("user_success_screen")
                },
                navController = navController
            )
        }
        composable("user_success_screen") {
            UserSuccessScreen(
                onNavigateToGoalScreen = {
                    navController.navigate("user_goal_type_screen")
                },
            )
        }
        composable("user_goal_type_screen") {
            UserGoalTypeScreen(
                viewModel = goalViewModel,
                onNextClick = {
                    navController.navigate("user_desire_weight_screen")
                },
            )
        }
        composable("user_desire_weight_screen") {
            UserDesiredWeightScreen(
                viewModel = goalViewModel,
                onNextClick = {
                    navController.navigate("user_weeks_screen")
                },
            )
        }
        composable("user_weeks_screen") {
            UserWeeksScreen(
                viewModel = goalViewModel,
                onCreateGoalClick = {
                    navController.navigate("user_main_graph") {
                        popUpTo("user_registration_graph") { inclusive = true }
                    }
                },
            )
        }
    }
}
