package com.example.nutritrack.screens.registration.user.create_goal

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.nutritrack.data.user.UserGoalViewModel

@Composable
fun UserDesiredWeightScreen(
    viewModel: UserGoalViewModel,
    onNextClick: () -> Unit
) {
    val weights = (30..200).toList()
    val selectedDesiredWeight = remember { mutableStateOf(73) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.progress_bar_step4),
                contentDescription = "Check icon",
                modifier = Modifier.size(230.dp)
            )

            Text(
                text = stringResource(R.string.specify_your_desired_weight),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.your_desired_weight_is_the_main_thing_to_create_a_plan_for_you),
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
                                val currentIndex = weights.indexOf(selectedDesiredWeight.value)
                                val newIndex =
                                    (currentIndex - unitsToChange).coerceIn(0, weights.size - 1)
                                selectedDesiredWeight.value = weights[newIndex]
                                accumulatedDrag -= unitsToChange * pixelsPerUnit
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.kg2, selectedDesiredWeight.value),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                viewModel.setTargetWeight(selectedDesiredWeight.value)
                onNextClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2F4F4F)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.Сontinue),
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}