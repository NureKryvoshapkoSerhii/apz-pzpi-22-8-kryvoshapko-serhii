package com.example.nutritrack.screens.registration.user.create_goal

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.user.UserGoalViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun UserWeeksScreen(
    viewModel: UserGoalViewModel,
    onCreateGoalClick: () -> Unit
) {
    val weeks = (1..144).toList()
    val selectedWeeks = remember { mutableStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF64A79B))
                .padding(16.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.progress_bar_step6),
                    contentDescription = "Progress bar step 6",
                    modifier = Modifier.size(230.dp)
                )

                Text(
                    text = stringResource(R.string.specify_the_number_of_weeks),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.specify_the_number_of_weeks_in_which_you_want_to_achieve_the_goal),
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 49.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .background(Color(0xFF2F4F4F), shape = RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            var accumulatedDrag = 0f
                            val pixelsPerUnit = 30f
                            detectVerticalDragGestures { change, dragAmount ->
                                accumulatedDrag += dragAmount
                                val unitsToChange = (accumulatedDrag / pixelsPerUnit).toInt()
                                if (unitsToChange != 0) {
                                    val currentIndex = weeks.indexOf(selectedWeeks.value)
                                    val newIndex =
                                        (currentIndex - unitsToChange).coerceIn(0, weeks.size - 1)
                                    selectedWeeks.value = weeks[newIndex]
                                    accumulatedDrag -= unitsToChange * pixelsPerUnit
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.weeks, selectedWeeks.value),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.setDurationWeeks(selectedWeeks.value)

                    coroutineScope.launch {
                        try {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            if (firebaseUser != null) {
                                val idToken = firebaseUser.getIdToken(false).await().token
                                if (idToken != null) {
                                    Log.d("UserWeeksScreen", "idToken: $idToken")
                                    viewModel.setIdToken(idToken)

                                    Log.d(
                                        "UserWeeksScreen",
                                        "Goal Data: ${viewModel.userData.value}"
                                    )

                                    val goalIds = ApiService.getAllUserGoalIds(idToken)
                                    if (goalIds.isNotEmpty()) {
                                        val oldGoalId = goalIds.first().goalId
                                        val deleteSuccess =
                                            ApiService.deleteGoal(oldGoalId, idToken)
                                        if (!deleteSuccess) {
                                            snackbarHostState.showSnackbar(
                                                message = "Error: Could not delete old goal",
                                                duration = SnackbarDuration.Long
                                            )
                                            return@launch
                                        }
                                    }

                                    val success =
                                        ApiService.createUserGoal(viewModel.userData.value)
                                    if (success) {
                                        viewModel.clearData()
                                        onCreateGoalClick()
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = "Error: Could not create a target",
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = "Error: Unable to retrieve idToken",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            } else {
                                snackbarHostState.showSnackbar(
                                    message = "Error: User is not authorized",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("UserWeeksScreen", "Failed to get idToken: $e")
                            snackbarHostState.showSnackbar(
                                message = "Error: $e",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_a_goal),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}