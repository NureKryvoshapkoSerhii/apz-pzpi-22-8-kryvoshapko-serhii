package com.example.nutritrack.screens.registration.consultant

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
import com.example.nutritrack.data.consultant.ConsultantRegistrationViewModel

@Composable
fun ConsultantExperienceYearsScreen(
    viewModel: ConsultantRegistrationViewModel,
    onNextClick: () -> Unit,
) {
    val experienceYearsList = (0..50).toList()
    val selectedExperienceYears = remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B))
            .padding(16.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.progress_bar_step5),
            contentDescription = "Progress bar step 5",
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.TopCenter)
                .padding(top = 200.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 252.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.specify_your_experience),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.how_many_years_have_you_been_working_as_consultant),
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 40.dp),
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
                                    val currentIndex =
                                        experienceYearsList.indexOf(selectedExperienceYears.value)
                                    val newIndex = (currentIndex - unitsToChange).coerceIn(
                                        0,
                                        experienceYearsList.size - 1
                                    )
                                    selectedExperienceYears.value = experienceYearsList[newIndex]
                                    accumulatedDrag -= unitsToChange * pixelsPerUnit
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.years, selectedExperienceYears.value),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.setExperienceYears(selectedExperienceYears.value)
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
                    text = stringResource(R.string.continue2),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}