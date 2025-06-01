package com.example.nutritrack.screens.registration.user.create_goal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R
import com.example.nutritrack.data.user.UserGoalViewModel

@Composable
fun UserGoalTypeScreen(
    viewModel: UserGoalViewModel,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.progress_bar_step2),
            contentDescription = "Check icon",
            modifier = Modifier.size(230.dp)
        )

        Text(
            text = stringResource(R.string.what_is_your_goal),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 25.dp, bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.choose_the_type_of_your_goal_so_that_we_can_calculate_everything_for_you),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 80.dp),
            textAlign = TextAlign.Center
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Button(
                onClick = {
                    viewModel.setGoalType(2)
                    onNextClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F4F4F)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.maintain_current_weight),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    viewModel.setGoalType(1)
                    onNextClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(63.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F4F4F)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.lose_weight),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    viewModel.setGoalType(0)
                    onNextClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2F4F4F)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.gain_weight),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}