package com.example.nutritrack.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.screens.user.ConsultantProfileScreen
import com.example.nutritrack.screens.user.ConsultantsScreen
import com.example.nutritrack.screens.user.DatePickerScreen
import com.example.nutritrack.screens.user.FoodSearchScreen
import com.example.nutritrack.screens.user.HistoryScreen
import com.example.nutritrack.screens.user.MealDetailsScreen
import com.example.nutritrack.screens.user.UserMainScreen
import com.example.nutritrack.screens.user.UserProfileScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.userMainNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = "user_main_screen",
        route = "user_main_graph"
    ) {
        composable("user_main_screen") {
            UserMainScreen(
                onLogoutClick = {
                    FirebaseAuthHelper.signOut()
                    navController.navigate("welcome_screen") {
                        popUpTo("user_main_graph") { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate("user_profile_screen")
                },
                onAddFoodClick = { mealType ->
                    navController.navigate("food_search_screen/$mealType")
                },
                onViewMealDetails = { mealType, selectedDate ->
                    navController.navigate("meal_details_screen/$mealType/$selectedDate")
                },
                onCalendarClick = {
                    navController.navigate("date_picker_screen")
                },
                onConsultantsClick = {
                    navController.navigate("consultants_screen")
                },
            )
        }

        composable(
            "meal_details_screen/{mealType}/{selectedDate}",
            arguments = listOf(
                navArgument("mealType") { type = NavType.StringType },
                navArgument("selectedDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: ""
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
            MealDetailsScreen(
                mealType = mealType,
                selectedDate = selectedDate,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("user_profile_screen") {
            UserProfileScreen(
                onBackClick = {
                    navController.navigateUp()
                },
                onSuccessScreenClick = {
                    navController.navigate("user_success_screen")
                },
                onNotebookClick = {
                    navController.navigate("user_main_screen") {
                        popUpTo("user_main_graph") { inclusive = true }
                    }
                },
                onConsultantsClick = {
                    navController.navigate("consultants_screen")
                }
            )
        }
    }

    composable("food_search_screen/{mealType}") { backStackEntry ->
        val mealType = backStackEntry.arguments?.getString("mealType") ?: ""
        FoodSearchScreen(
            mealType = mealType,
            onBackClick = {
                navController.navigateUp()
            },
            onFoodAdded = { consumedFood ->
                navController.navigateUp()
            }
        )
    }

    composable("date_picker_screen") {
        DatePickerScreen(
            onDateSelected = { selectedDate ->
                navController.navigate("history_screen/$selectedDate") {
                    popUpTo("date_picker_screen") {
                        inclusive = true
                    }
                }
            },
            onBackClick = { navController.popBackStack() }
        )
    }
    composable(
        "history_screen/{selectedDate}",
        arguments = listOf(navArgument("selectedDate") { type = NavType.StringType })
    ) { backStackEntry ->
        val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
        HistoryScreen(
            selectedDate = selectedDate,
            onBackClick = { navController.popBackStack() },
            onAddFoodClick = { mealType ->
                navController.navigate("food_search_screen/$mealType")
            },
            onViewMealDetails = { mealType, date ->
                navController.navigate("meal_details_screen/$mealType/$date")
            }
        )
    }

    composable("consultants_screen") {
        ConsultantsScreen(
            onBackClick = { navController.popBackStack() },
            onNotebookClick = {
                navController.navigate("user_main_screen") {
                    popUpTo("user_main_graph") { inclusive = true }
                }
            },
            onProfileClick = {
                navController.navigate("user_profile_screen")
            },
            onConsultantProfileClick = { consultantUid ->
                navController.navigate("consultant_profile_screen/$consultantUid")
            }
        )
    }

    composable(
        "consultant_profile_screen/{consultantUid}",
        arguments = listOf(navArgument("consultantUid") { type = NavType.StringType })
    ) { backStackEntry ->
        val consultantUid = backStackEntry.arguments?.getString("consultantUid") ?: ""
        ConsultantProfileScreen(
            consultantUid = consultantUid,
            onBackClick = { navController.popBackStack() },
            onRequestSent = {
                // Поки що повернемося назад після надсилання запиту
                navController.popBackStack()
            }
        )
    }
}