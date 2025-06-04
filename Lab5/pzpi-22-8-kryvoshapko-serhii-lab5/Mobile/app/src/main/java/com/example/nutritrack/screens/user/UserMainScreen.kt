package com.example.nutritrack.screens.user

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.model.GoalResponse
import com.example.nutritrack.model.MealEntry
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddFoodClick: (String) -> Unit,
    onViewMealDetails: (String, String) -> Unit,
    onCalendarClick: () -> Unit,
    onConsultantsClick: () -> Unit,
) {
    var goalData by remember { mutableStateOf<GoalResponse?>(null) }
    var mealEntries by remember { mutableStateOf<List<MealEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var streak by remember { mutableStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val caloriesAnimationProgress = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("NutriTrackPrefs", Context.MODE_PRIVATE)
    val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun saveLastLoginDate(date: String) {
        sharedPreferences.edit().putString("lastLoginDate", date).apply()
    }

    fun getLastLoginDate(): String? {
        return sharedPreferences.getString("lastLoginDate", null)
    }

    fun saveStreak(value: Int) {
        sharedPreferences.edit().putInt("currentStreak", value).apply()
    }

    fun getStreak(): Int {
        return sharedPreferences.getInt("currentStreak", 0)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                val idToken = FirebaseAuthHelper.getIdToken()
                if (idToken == null) {
                    errorMessage = context.getString(R.string.authorization_error_idtoken_not_found)
                    return@launch
                }

                val lastLoginDateStr = getLastLoginDate()
                val currentDate = LocalDate.now()
                var currentStreak: Int

                val streakResponse = ApiService.getMostRecentStreak(idToken)

                if (streakResponse == null && lastLoginDateStr == null) {
                    currentStreak = 1
                    if (ApiService.addStreak(idToken, currentStreak)) {
                        saveStreak(currentStreak)
                        saveLastLoginDate(currentDate.toString())
                    } else {
                        errorMessage = context.getString(R.string.failed_to_create_streak)
                        return@launch
                    }
                } else if (streakResponse == null) {
                    currentStreak = 1
                    if (ApiService.addStreak(idToken, currentStreak)) {
                        saveStreak(currentStreak)
                        saveLastLoginDate(currentDate.toString())
                    } else {
                        errorMessage = context.getString(R.string.failed_to_create_streak2)
                        return@launch
                    }
                } else {
                    currentStreak = streakResponse.current_streak
                    val streakDate = LocalDate.parse(
                        streakResponse.streak_date.split("T")[0],
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    val lastLoginDate = lastLoginDateStr?.let {
                        LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                    } ?: streakDate

                    val daysDifference = ChronoUnit.DAYS.between(lastLoginDate, currentDate).toInt()

                    when {
                        daysDifference == 0 -> {
                            currentStreak = streakResponse.current_streak
                            saveStreak(currentStreak)
                        }

                        daysDifference == 1 -> {
                            currentStreak = streakResponse.current_streak + 1
                            if (ApiService.updateStreak(idToken, currentStreak, true)) {
                                saveStreak(currentStreak)
                                saveLastLoginDate(currentDate.toString())
                            } else {
                                errorMessage = context.getString(R.string.failed_to_update_streak)
                                return@launch
                            }
                        }

                        daysDifference >= 2 -> {
                            currentStreak = 0
                            if (ApiService.updateStreak(idToken, currentStreak, false)) {
                                saveStreak(currentStreak)
                                saveLastLoginDate(currentDate.toString())
                            } else {
                                errorMessage = context.getString(R.string.failed_to_update_streak)
                                return@launch
                            }
                        }
                    }
                }
                streak = currentStreak

                val goalIds = ApiService.getAllUserGoalIds(idToken)
                if (goalIds.isEmpty()) {
                    errorMessage = context.getString(R.string.target_not_found)
                    return@launch
                }

                val goalId = goalIds.first().goalId
                val goal = ApiService.getSpecificGoalById(goalId)
                if (goal == null) {
                    errorMessage = context.getString(R.string.unable_to_obtain_target_details)
                    return@launch
                }
                goalData = goal

                val meals = ApiService.getAllMeals(idToken)
                mealEntries = meals.filter {
                    val entryDate = LocalDate.parse(
                        it.entry_date.split("T")[0],
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    entryDate.isEqual(LocalDate.now())
                }

                caloriesAnimationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(100, easing = LinearEasing)
                )
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.logout_confirmation),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.are_you_sure_you_want_to_log_out),
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            containerColor = Color(0xFF2F4F4F),
            shape = RoundedCornerShape(12.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.yes), fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.no), fontSize = 16.sp)
                }
            }
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logout),
                                contentDescription = "Logout",
                                modifier = Modifier.size(35.dp),
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "NutriTrack",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    Text(
                        text = streak.toString(),
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = { /* TODO: Action for flame icon */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_flame),
                            contentDescription = "Fire",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onCalendarClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Calendar",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2F4F4F)
                ),
                modifier = Modifier.height(75.dp)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF2F4F4F),
                modifier = Modifier.height(102.dp)
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* TODO: Action for "Notebook" */ },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notebook),
                            contentDescription = "Notebook",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.notebook),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color(0xFF64A79B)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onConsultantsClick() },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_communication),
                            contentDescription = "Consultants",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.consultants),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color(0xFF64A79B)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onProfileClick() },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.profile),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color(0xFF64A79B)
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF64A79B))
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else if (goalData != null) {
                Text(
                    text = stringResource(R.string.today),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = stringResource(R.string.the_number_of_calories),
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val totalConsumedCalories = mealEntries.sumOf { it.calories }
                val totalGoalCalories = goalData!!.dailyCalories.toInt()
                val remainingCalories = totalGoalCalories - totalConsumedCalories.toInt()
                val progressTarget =
                    (totalConsumedCalories.toFloat() / totalGoalCalories).coerceIn(0f, 1f)
                val progress by animateFloatAsState(
                    targetValue = progressTarget * caloriesAnimationProgress.value,
                    animationSpec = tween(durationMillis = 100),
                    label = "Calories Progress Animation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(175.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension
                        val radius = (diameter / 2) - (strokeWidth / 2)
                        val startAngle = -90f
                        val sweepAngle = 360f

                        drawArc(
                            color = Color.White.copy(alpha = 0.3f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle * progress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (totalConsumedCalories.toInt() == 0) {
                                totalGoalCalories.toString()
                            } else {
                                remainingCalories.toString()
                            },
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (totalConsumedCalories.toInt() == 0) {
                                stringResource(
                                    R.string.daily_goal_remaining,
                                    totalGoalCalories,
                                    totalGoalCalories
                                )
                            } else {
                                stringResource(
                                    R.string.consumed_remaining,
                                    totalConsumedCalories,
                                    remainingCalories
                                )
                            },
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                val totalProtein = mealEntries.sumOf { it.protein }
                val totalFats = mealEntries.sumOf { it.fats }
                val totalCarbs = mealEntries.sumOf { it.carbs }

                val proteinProgressTarget =
                    (totalProtein / goalData!!.dailyProtein).toFloat().coerceIn(0f, 1f)
                val fatsProgressTarget =
                    (totalFats / goalData!!.dailyFats).toFloat().coerceIn(0f, 1f)
                val carbsProgressTarget =
                    (totalCarbs / goalData!!.dailyCarbs).toFloat().coerceIn(0f, 1f)

                val proteinProgress by animateFloatAsState(
                    targetValue = proteinProgressTarget * caloriesAnimationProgress.value,
                    animationSpec = tween(durationMillis = 100),
                    label = "Protein Progress Animation"
                )
                val fatsProgress by animateFloatAsState(
                    targetValue = fatsProgressTarget * caloriesAnimationProgress.value,
                    animationSpec = tween(durationMillis = 100),
                    label = "Fats Progress Animation"
                )
                val carbsProgress by animateFloatAsState(
                    targetValue = carbsProgressTarget * caloriesAnimationProgress.value,
                    animationSpec = tween(durationMillis = 100),
                    label = "Carbs Progress Animation"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(80.dp * proteinProgress)
                                    .background(Color.White)
                            )
                        }

                        Text(
                            text = stringResource(
                                R.string.proteins,
                                totalProtein.toInt(),
                                goalData!!.dailyProtein.toInt()
                            ),
                            fontSize = 12.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(80.dp * fatsProgress)
                                    .background(Color.White)
                            )
                        }

                        Text(
                            text = stringResource(
                                R.string.fats,
                                totalFats.toInt(),
                                goalData!!.dailyFats.toInt()
                            ),
                            fontSize = 12.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(80.dp * carbsProgress)
                                    .background(Color.White)
                            )
                        }

                        Text(
                            text = stringResource(
                                R.string.carbs,
                                totalCarbs.toInt(),
                                goalData!!.dailyCarbs.toInt()
                            ),
                            fontSize = 12.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.today_s_diet),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                val mealCalories = mealEntries.groupBy { it.meal_type }
                    .mapValues { it.value.sumOf { meal -> meal.calories } }
                val breakfastCalories = mealCalories[stringResource(R.string.breakfast_card)] ?: 0
                val lunchCalories = mealCalories[stringResource(R.string.lunch_card)] ?: 0
                val dinnerCalories = mealCalories[stringResource(R.string.dinner_card)] ?: 0
                val snackCalories = mealCalories[stringResource(R.string.snack_card)] ?: 0

                val totalCalories = goalData!!.dailyCalories
                val breakfastGoal = (totalCalories * 0.3).toInt()
                val lunchGoal = (totalCalories * 0.35).toInt()
                val dinnerGoal = (totalCalories * 0.25).toInt()
                val snackGoal = (totalCalories * 0.1).toInt()

                val meals = listOf(
                    Triple(
                        stringResource(R.string.breakfast),
                        R.drawable.ic_breakfast,
                        breakfastCalories to breakfastGoal
                    ),
                    Triple(
                        stringResource(R.string.lunch),
                        R.drawable.ic_lunch,
                        lunchCalories to lunchGoal
                    ),
                    Triple(
                        stringResource(R.string.dinner),
                        R.drawable.ic_dinner,
                        dinnerCalories to dinnerGoal
                    ),
                    Triple(
                        stringResource(R.string.snack),
                        R.drawable.ic_snack,
                        snackCalories to snackGoal
                    )
                )
                meals.forEach { (meal, iconRes, caloriesPair) ->
                    val (consumed, goal) = caloriesPair
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable {
                                onViewMealDetails(
                                    meal.lowercase(),
                                    currentDate
                                )
                            }, // Передаємо поточну дату
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2F4F4F)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = iconRes),
                                    contentDescription = meal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = meal,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(R.string.cal, consumed.toInt(), goal),
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(
                                onClick = { onAddFoodClick(meal.lowercase()) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_add),
                                    contentDescription = "Add",
                                    modifier = Modifier.size(30.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}