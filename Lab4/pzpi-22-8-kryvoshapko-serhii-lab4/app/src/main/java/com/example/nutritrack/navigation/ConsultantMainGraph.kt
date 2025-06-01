package com.example.nutritrack.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.screens.consultant.ConsultantMainScreen
import com.example.nutritrack.screens.consultant.ConsultantProfileScreen
import com.example.nutritrack.screens.consultant.ConsultantSearchScreen
import com.example.nutritrack.screens.consultant.ConsultantSuccessScreen
import com.example.nutritrack.screens.consultant.UserMealScreen
import com.example.nutritrack.screens.consultant.UserProfileScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.consultantMainNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = "consultant_main_screen",
        route = "consultant_main_graph"
    ) {
        composable("consultant_main_screen") {
            ConsultantMainScreen(
                onLogoutClick = {
                    FirebaseAuthHelper.signOut()
                    navController.navigate("welcome_screen") {
                        popUpTo("consultant_main_graph") { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate("consultant_profile_screen")
                },
                onSearchClick = {
                    navController.navigate("consultant_search_screen")
                },
                onUserMealClick = { userUid ->
                    navController.navigate("consultant_user_meal_screen/$userUid")
                }
            )
        }
        composable("consultant_user_profile_screen/{userUid}") { backStackEntry ->
            val userUid = backStackEntry.arguments?.getString("userUid") ?: ""
            UserProfileScreen(
                userUid = userUid,
                onBackClick = {
                    navController.navigate("consultant_search_screen") {
                        popUpTo("consultant_user_profile_screen/$userUid") { inclusive = true }
                    }
                },
                onClientAdded = {
                    // Поки що нічого не робимо, але можна додати логіку оновлення
                })
        }
        composable("consultant_user_meal_screen/{userUid}") { backStackEntry ->
            val userUid = backStackEntry.arguments?.getString("userUid") ?: ""
            UserMealScreen(
                userUid = userUid,
                onBackClick = {
                    navController.navigate("consultant_main_screen") {
                        popUpTo("consultant_user_meal_screen/$userUid") { inclusive = true }
                    }
                }
            )
        }

        composable("consultant_search_screen") {
            ConsultantSearchScreen(
                onProfileClick = {
                    navController.navigate("consultant_profile_screen")
                },
                onHomeClick = {
                    navController.navigate("consultant_main_screen") {
                        popUpTo("consultant_main_graph") { inclusive = false }
                    }
                },
                onUserProfileClick = { userUid ->
                    navController.navigate("consultant_user_profile_screen/$userUid")
                }
            )
        }
        composable("consultant_success_screen") {
            ConsultantSuccessScreen(
                onNavigateToMainScreen = {
                    FirebaseAuthHelper.signOut()
                    navController.navigate("welcome_screen") {
                        popUpTo("consultant_main_graph") { inclusive = true }
                    }
                }
            )
        }
    }
    composable("consultant_profile_screen") {
        ConsultantProfileScreen(
            onHomeClick = {
                navController.navigate("consultant_main_screen") {
                    popUpTo("consultant_profile_screen") { inclusive = true }
                }
            },
            onSearchClick = {
                navController.navigate("consultant_search_screen") {
                    popUpTo("consultant_profile_screen") { inclusive = true }
                }
            },
            onProfileClick = {
                // Залишаємося на тому ж екрані, оскільки це профіль
            }
        )
    }
}