package com.example.nutritrack.screens.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.model.Consultant
import com.example.nutritrack.model.ConsultantRequest
import com.example.nutritrack.model.LinkedRelationship
import com.example.nutritrack.util.LocalStorageUtil
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultantsScreen(
    onBackClick: () -> Unit,
    onNotebookClick: () -> Unit,
    onProfileClick: () -> Unit,
    onConsultantProfileClick: (String) -> Unit
) {
    var consultants by remember { mutableStateOf<List<Consultant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var linkedRelationship by remember { mutableStateOf<LinkedRelationship?>(null) }
    var requests by remember { mutableStateOf<List<ConsultantRequest>>(emptyList()) }
    var showRequestsDialog by remember { mutableStateOf(false) }
    var isResponding by remember { mutableStateOf(false) }
    var respondError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val idToken = FirebaseAuthHelper.getIdToken()
                if (idToken == null) {
                    errorMessage = "Authorization error: IdToken not found"
                    return@launch
                }

                val relationships = ApiService.getLinkedRelationships(idToken)
                linkedRelationship = relationships.firstOrNull { it.isActive }

                if (linkedRelationship == null) {
                    val response = ApiService.getAllConsultants(idToken)
                    consultants = response

                    // Завантажуємо всі запити
                    val allRequests =
                        ApiService.getAllRequests(idToken).filter { it.status == "pending" }
                    // Читаємо метадані з локального файлу, використовуючи переданий context
                    val metadata = LocalStorageUtil.readRequestMetadata(context)
                    // Фільтруємо запити, де ініціатор — "consultant"
                    requests = allRequests.filter { request ->
                        val requestMetadata = metadata.find { it.requestId == request.requestId }
                        requestMetadata?.initiator == "consultant"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    if (linkedRelationship != null) {
        ConsultantNotesScreen(
            linkedRelationship = linkedRelationship!!,
            onBackClick = onBackClick,
            onNotebookClick = onNotebookClick,
            onProfileClick = onProfileClick,
            onConsultantProfileClick = onConsultantProfileClick
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF2F4F4F),
                    modifier = Modifier.height(102.dp)
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNotebookClick() },
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
                        selected = true,
                        onClick = { /* Already on consultants screen */ },
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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.consultants),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    if (requests.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clickable { showRequestsDialog = true }
                                .background(Color(0xFF2F4F4F), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_requests),
                                    contentDescription = "Requests",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "${requests.size}",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    placeholder = {
                        Text(
                            stringResource(R.string.search_by_nickname),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2F4F4F),
                        unfocusedContainerColor = Color(0xFF2F4F4F),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* Handle search if needed */ }),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
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
                } else {
                    val filteredConsultants = if (searchQuery.isNotEmpty()) {
                        consultants.filter { it.nickname.contains(searchQuery, ignoreCase = true) }
                    } else {
                        consultants
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredConsultants) { consultant ->
                            ConsultantCard(
                                consultant = consultant,
                                onClick = { onConsultantProfileClick(consultant.consultant_uid) }
                            )
                        }
                    }
                }
            }
        }

        if (showRequestsDialog) {
            AlertDialog(
                onDismissRequest = { showRequestsDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.pending_requests),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        if (requests.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_pending_requests),
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(requests) { request ->
                                    RequestCard(
                                        request = request,
                                        onAccept = {
                                            scope.launch {
                                                isResponding = true
                                                respondError = null
                                                val idToken = FirebaseAuthHelper.getIdToken()
                                                if (idToken != null) {
                                                    val success = ApiService.userRespondInvite(
                                                        idToken,
                                                        request.consultantUid,
                                                        true
                                                    )
                                                    if (success) {
                                                        requests =
                                                            requests.filter { it.requestId != request.requestId }
                                                        val relationships =
                                                            ApiService.getLinkedRelationships(
                                                                idToken
                                                            )
                                                        linkedRelationship =
                                                            relationships.firstOrNull { it.isActive }
                                                    } else {
                                                        respondError = "Failed to accept request"
                                                    }
                                                } else {
                                                    respondError = "Failed to get idToken"
                                                }
                                                isResponding = false
                                            }
                                        },
                                        onDecline = {
                                            scope.launch {
                                                isResponding = true
                                                respondError = null
                                                val idToken = FirebaseAuthHelper.getIdToken()
                                                if (idToken != null) {
                                                    val success = ApiService.userRespondInvite(
                                                        idToken,
                                                        request.consultantUid,
                                                        false
                                                    )
                                                    if (success) {
                                                        requests =
                                                            requests.filter { it.requestId != request.requestId }
                                                    } else {
                                                        respondError = "Failed to decline request"
                                                    }
                                                } else {
                                                    respondError = "Failed to get idToken"
                                                }
                                                isResponding = false
                                            }
                                        },
                                        isResponding = isResponding
                                    )
                                }
                            }
                        }

                        if (respondError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = respondError!!,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                containerColor = Color(0xFF2F4F4F),
                shape = RoundedCornerShape(12.dp),
                confirmButton = {
                    TextButton(
                        onClick = { showRequestsDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.close), fontSize = 16.sp)
                    }
                },
                dismissButton = {}
            )
        }
    }
}

@Composable
fun ConsultantCard(consultant: Consultant, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F4F4F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (consultant.profile_picture.isNotEmpty()) {
                    AsyncImage(
                        model = consultant.profile_picture,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "No Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = consultant.nickname,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.experience_years, consultant.experience_years),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(
                        R.string.clients,
                        consultant.current_clients,
                        consultant.max_clients
                    ),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                val availableSlots = consultant.max_clients - consultant.current_clients
                Text(
                    text = stringResource(R.string.available_slots, availableSlots),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RequestCard(
    request: ConsultantRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    isResponding: Boolean
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF64A79B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (request.consultant.profile_picture.isNotEmpty()) {
                        AsyncImage(
                            model = request.consultant.profile_picture,
                            contentDescription = "Consultant Picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "No Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = request.consultant.nickname,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(
                            R.string.experience_years2,
                            request.consultant.experience_years
                        ),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onAccept()
                        LocalStorageUtil.removeRequestMetadata(context, request.requestId)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                    enabled = !isResponding
                ) {
                    if (isResponding) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.accept),
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                Button(
                    onClick = {
                        onDecline()
                        LocalStorageUtil.removeRequestMetadata(context, request.requestId)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                    enabled = !isResponding
                ) {
                    if (isResponding) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.decline),
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}