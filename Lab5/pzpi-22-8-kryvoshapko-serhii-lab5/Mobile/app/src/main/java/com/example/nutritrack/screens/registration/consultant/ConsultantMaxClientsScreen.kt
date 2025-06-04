package com.example.nutritrack.screens.registration.consultant

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.GoogleAuth
import com.example.nutritrack.data.consultant.ConsultantRegistrationViewModel
import kotlinx.coroutines.launch

@Composable
fun ConsultantMaxClientsScreen(
    onRegistrationSuccess: () -> Unit,
    viewModel: ConsultantRegistrationViewModel,
    navController: NavHostController,
) {
    val maxClientsList = (1..50).toList()
    val selectedMaxClients = remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        GoogleAuth.initialize(context)
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            coroutineScope.launch {
                try {
                    val idToken = GoogleAuth.handleSignInResult(result.data)
                    viewModel.setIdToken(idToken)

                    val consultantExist = ApiService.checkConsultantExists(idToken)
                    val userExist = ApiService.checkUserExists(idToken)
                    if (userExist) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.error_you_can_t_create_an_account_because_you_re_already_registered_as_a_user_at_this_email_address),
                            duration = SnackbarDuration.Short

                        )
                        navController.navigate("welcome_screen") {
                            popUpTo("user_nickname_screen") { inclusive = true }
                        }
                    } else if (consultantExist) {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.error_the_consultant_with_this_account_is_already_registered_please_log_in),
                            duration = SnackbarDuration.Short
                        )
                        navController.navigate("welcome_screen") {
                            popUpTo("user_nickname_screen") { inclusive = true }
                        }
                    } else {
                        val success = ApiService.registerConsultant(viewModel.consultantData.value)
                        if (success) {
                            viewModel.clearData()
                            onRegistrationSuccess()
                        } else {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.error_could_not_register_a_consultant),
                                duration = SnackbarDuration.Long
                            )
                        }
                    }

                } catch (e: Exception) {
                    Log.e("ConsultantMaxClientsScreen", "Google Sign-In failed: $e")
                    snackbarHostState.showSnackbar(
                        message = "Authorization error: $e",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF64A79B))
                .padding(16.dp)
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.progress_bar_step6),
                contentDescription = "Progress bar step 6",
                modifier = Modifier
                    .size(580.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 120.dp, bottom = 220.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.specify_the_number_of_clients),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.how_many_clients_can_you_take_on_at_once),
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
                                            maxClientsList.indexOf(selectedMaxClients.value)
                                        val newIndex = (currentIndex - unitsToChange).coerceIn(
                                            0,
                                            maxClientsList.size - 1
                                        )
                                        selectedMaxClients.value = maxClientsList[newIndex]
                                        accumulatedDrag -= unitsToChange * pixelsPerUnit
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.clients4, selectedMaxClients.value),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.setMaxClients(selectedMaxClients.value)
                        GoogleAuth.signIn(launcher)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.register),
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}