package com.example.nutritrack.screens.user

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    selectedDate: String,
    onBackClick: () -> Unit,
    onAddFoodClick: (String) -> Unit,
    onViewMealDetails: (String, String) -> Unit
) {
    var goalData by remember { mutableStateOf<GoalResponse?>(null) }
    var mealEntries by remember { mutableStateOf<List<MealEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val caloriesAnimationProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDate) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                val idToken = FirebaseAuthHelper.getIdToken()
                if (idToken == null) {
                    errorMessage = "Authorization error: IdToken not found"
                    return@launch
                }

                val goalIds = ApiService.getAllUserGoalIds(idToken)
                if (goalIds.isEmpty()) {
                    errorMessage = "Target not found"
                    return@launch
                }

                val goalId = goalIds.first().goalId
                val goal = ApiService.getSpecificGoalById(goalId)
                if (goal == null) {
                    errorMessage = "Unable to obtain target details"
                    return@launch
                }
                goalData = goal

                val meals = ApiService.getAllMeals(idToken)
                val targetDate = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                mealEntries = meals.filter {
                    val entryDate = LocalDate.parse(
                        it.entry_date.split("T")[0],
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    entryDate.isEqual(targetDate)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2F4F4F)
                ),
                modifier = Modifier.height(75.dp)
            )
        },
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
                    text = LocalDate.parse(selectedDate, DateTimeFormatter.ISO_LOCAL_DATE)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = stringResource(R.string.the_number_of_calories2),
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
                                    R.string.daily_goal_remaining2,
                                    totalGoalCalories,
                                    totalGoalCalories
                                )
                            } else {
                                stringResource(
                                    R.string.consumed_remaining2,
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
                                R.string.proteins2,
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
                                R.string.fats2,
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
                                R.string.carbs3,
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
                    text = stringResource(R.string.diet_for_this_day),
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
                val lunchGoal = (totalCalories * 0.3).toInt()
                val dinnerGoal = (totalCalories * 0.3).toInt()
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
                                    selectedDate
                                )
                            },
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
                                    text = stringResource(R.string.cal2, consumed.toInt(), goal),
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(
                                onClick = { onAddFoodClick(meal.lowercase()) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_add),
                                    contentDescription = stringResource(R.string.add),
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