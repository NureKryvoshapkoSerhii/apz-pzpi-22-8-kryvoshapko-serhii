package com.example.nutritrack.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.data.auth.GoogleAuth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onNewUserClick: () -> Unit,
    onConsultantClick: () -> Unit,
    onLoginAsUserClick: () -> Unit,
    onLoginAsConsultantClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        GoogleAuth.initialize(context)
    }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var idToken by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState()
    val showBottomSheet = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        scope.launch {
            isLoading = true
            try {
                idToken = GoogleAuth.handleSignInResult(result.data)
                if (idToken == null) {
                    errorMessage = "Authorization error: Unable to retrieve token"
                } else {
                    showBottomSheet.value = true
                }
            } catch (e: Exception) {
                errorMessage = "Authorization error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_icon),
            contentDescription = "Food plate illustration",
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = "NutriTrack",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6E6E6),
            modifier = Modifier.padding(top = 25.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Button(
                onClick = onNewUserClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E9393)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_user),
                    fontSize = 21.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = onConsultantClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E9393)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_consultant),
                    fontSize = 21.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    GoogleAuth.signIn(launcher)
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
                    text = stringResource(R.string.i_already_have_an_account),
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet.value = false
                FirebaseAuth.getInstance().signOut()
            },
            sheetState = sheetState,
            containerColor = Color(0xFF2F4F4F)
        ) {
            var bottomSheetLoading by remember { mutableStateOf(false) }
            var bottomSheetError by remember { mutableStateOf<String?>(null) }

            bottomSheetError?.let { message ->
                LaunchedEffect(message) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    bottomSheetError = null
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_the_type_of_account),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            bottomSheetLoading = true
                            try {
                                if (idToken == null) {
                                    bottomSheetError =
                                        context.getString(R.string.wait_for_authorization_to_complete)
                                    return@launch
                                }

                                val result = FirebaseAuthHelper.loginAsUser()
                                result.fold(
                                    onSuccess = {
                                        sheetState.hide()
                                        showBottomSheet.value = false
                                        onLoginAsUserClick()
                                    },
                                    onFailure = { exception ->
                                        bottomSheetError =
                                            context.getString(
                                                R.string.user_not_found_try_logging_in_as_a_consultant,
                                                exception.message
                                            )
                                    }
                                )
                            } catch (e: Exception) {
                                bottomSheetError = context.getString(R.string.error, e.message)
                            } finally {
                                bottomSheetLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E9393)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !bottomSheetLoading
                ) {
                    if (bottomSheetLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.log_in_as_a_user),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            bottomSheetLoading = true
                            try {
                                if (idToken == null) {
                                    bottomSheetError =
                                        context.getString(R.string.wait_for_authorization_to_complete)
                                    return@launch
                                }

                                val result = FirebaseAuthHelper.loginAsConsultant()
                                result.fold(
                                    onSuccess = {
                                        sheetState.hide()
                                        showBottomSheet.value = false
                                        onLoginAsConsultantClick()
                                    },
                                    onFailure = { exception ->
                                        bottomSheetError =
                                            context.getString(
                                                R.string.consultant_not_found_try_logging_in_as_a_user,
                                                exception.message
                                            )
                                    }
                                )
                            } catch (e: Exception) {
                                bottomSheetError = context.getString(R.string.error, e.message)
                            } finally {
                                bottomSheetLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E9393)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !bottomSheetLoading
                ) {
                    if (bottomSheetLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.log_in_as_a_consultant),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}