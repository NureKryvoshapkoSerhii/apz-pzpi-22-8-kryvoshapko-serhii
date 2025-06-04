package com.example.nutritrack.screens.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.model.Consultant
import com.example.nutritrack.util.LocalStorageUtil
import com.example.nutritrack.util.RequestMetadata
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConsultantProfileScreen(
    consultantUid: String,
    onBackClick: () -> Unit,
    onRequestSent: () -> Unit
) {
    var consultant by remember { mutableStateOf<Consultant?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSendingInvite by remember { mutableStateOf(false) }
    var inviteError by remember { mutableStateOf<String?>(null) }
    var inviteSuccess by remember { mutableStateOf(false) }
    var isRemovingConsultant by remember { mutableStateOf(false) }
    var removeError by remember { mutableStateOf<String?>(null) }
    var hasLinkedRelationship by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var dialogAction by remember { mutableStateOf<String?>(null) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(consultantUid) {
        try {
            isLoading = true
            errorMessage = null
            val idToken = FirebaseAuthHelper.getIdToken() ?: run {
                errorMessage = "Authorization error: IdToken not found"
                return@LaunchedEffect
            }

            val relationships = ApiService.getLinkedRelationships(idToken)
            hasLinkedRelationship =
                relationships.any { it.isActive && it.consultantUid == consultantUid }

            val consultants = ApiService.getAllConsultants(idToken)
            consultant = consultants.find { it.consultant_uid == consultantUid }
            if (consultant == null) {
                errorMessage = "Consultant not found"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun sendRequestToConsultant(context: android.content.Context) {
        scope.launch {
            isSendingInvite = true
            inviteError = null
            inviteSuccess = false

            val idToken = FirebaseAuthHelper.getIdToken()
            if (idToken != null) {
                val success = ApiService.sendInviteToConsultant(idToken, consultantUid)
                if (success) {
                    inviteSuccess = true
                    onRequestSent()
                    // Зберігаємо метадані запиту
                    val requestId = ApiService.getAllRequests(idToken)
                        .filter { it.consultantUid == consultantUid && it.status == "pending" }
                        .maxByOrNull { it.requestId }?.requestId
                    if (requestId != null) {
                        LocalStorageUtil.saveRequestMetadata(
                            context, // Використовуємо переданий context
                            RequestMetadata(requestId, "user")
                        )
                    }
                } else {
                    inviteError = "You already sent an invitation"
                }
            } else {
                inviteError = "Failed to get idToken"
            }
            isSendingInvite = false
        }
    }

    fun removeConsultant() {
        scope.launch {
            isRemovingConsultant = true
            removeError = null

            val idToken = FirebaseAuthHelper.getIdToken()
            if (idToken != null) {
                val success = ApiService.removeConsultant(idToken, consultantUid)
                if (success) {
                    hasLinkedRelationship = false
                    onBackClick()
                } else {
                    removeError = "Failed to remove consultant"
                }
            } else {
                removeError = "Failed to get idToken"
            }
            isRemovingConsultant = false
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = dialogText,
                    color = Color.White,
                    fontSize = 16.sp
                )
            },
            containerColor = Color(0xFF2F4F4F),
            shape = RoundedCornerShape(12.dp),
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        when (dialogAction) {
                            "send_request" -> sendRequestToConsultant(context)
                            "remove_consultant" -> removeConsultant()
                        }
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
                    onClick = { showDialog = false },
                    colors = textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.no), fontSize = 16.sp)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(55.dp)
                )
            }
            Text(
                text = stringResource(R.string.consultant_profile),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.size(30.dp))
        }

        Spacer(modifier = Modifier.height(100.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else if (consultant != null) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (consultant!!.profile_picture.isNotEmpty()) {
                    AsyncImage(
                        model = consultant!!.profile_picture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "No Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(110.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = consultant!!.nickname,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F4F4F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.bio),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = consultant!!.profile_description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F4F4F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.clients2,
                            consultant!!.current_clients,
                            consultant!!.max_clients
                        ),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val availableSlots = consultant!!.max_clients - consultant!!.current_clients
                    Text(
                        text = stringResource(R.string.available_slots2, availableSlots),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(
                            R.string.experience_years3,
                            consultant!!.experience_years
                        ),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val genderLocalized = when (consultant?.gender?.lowercase()) {
                        "male" -> stringResource(R.string.gender_male)
                        "female" -> stringResource(R.string.gender_female)
                        else -> consultant?.gender ?: ""
                    }
                    Text(
                        text = stringResource(R.string.gender3, genderLocalized),
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (inviteError != null) {
                Text(
                    text = inviteError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            } else if (inviteSuccess) {
                Text(
                    text = stringResource(R.string.invite_sent_successfully),
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            } else if (removeError != null) {
                Text(
                    text = removeError!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            val context = LocalContext.current
            if (hasLinkedRelationship) {
                Button(
                    onClick = {
                        dialogAction = "remove_consultant"
                        dialogTitle = "Remove Consultant"
                        dialogText =
                            context.getString(R.string.are_you_sure_you_want_to_remove_this_consultant_this_action_cannot_be_undone)
                        showDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F4F4F)
                    ),
                    enabled = !isRemovingConsultant
                ) {
                    if (isRemovingConsultant) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.remove_consultant),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        dialogAction = "send_request"
                        dialogTitle = context.getString(R.string.send_request)
                        dialogText =
                            context.getString(R.string.are_you_sure_you_want_to_send_a_request_to_this_consultant)
                        showDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F4F4F)
                    ),
                    enabled = !isSendingInvite && !inviteSuccess && (consultant!!.max_clients - consultant!!.current_clients) > 0
                ) {
                    if (isSendingInvite) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.send_request),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}