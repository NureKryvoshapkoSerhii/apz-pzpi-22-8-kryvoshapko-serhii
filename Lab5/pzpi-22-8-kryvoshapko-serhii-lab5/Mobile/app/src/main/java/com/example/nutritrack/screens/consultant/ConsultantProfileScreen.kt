package com.example.nutritrack.screens.consultant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.model.Consultant
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultantProfileScreen(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxClients by remember { mutableStateOf(0) }
    var experienceYears by remember { mutableStateOf(0) }
    var consultantData by remember { mutableStateOf<Consultant?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            isUploading = true
            uploadError = null

            val storageRef = FirebaseStorage.getInstance().reference
            val fileName =
                "profile_pictures/${FirebaseAuthHelper.getUid()}/${UUID.randomUUID()}.jpg"
            val photoRef = storageRef.child(fileName)

            photoRef.putFile(uri)
                .addOnSuccessListener {
                    photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        scope.launch {
                            val idToken = FirebaseAuthHelper.getIdToken()
                            if (idToken != null) {
                                val success = ApiService.updateConsultantProfilePicture(
                                    idToken,
                                    downloadUri.toString()
                                )
                                if (success) {
                                    val idToken = FirebaseAuthHelper.getIdToken()
                                    if (idToken != null) {
                                        val consultants = ApiService.getAllConsultants(idToken)
                                        consultantData =
                                            consultants.find { it.consultant_uid == FirebaseAuthHelper.getUid() }
                                        profileImageUri = null
                                    }
                                } else {
                                    uploadError =
                                        context.getString(R.string.failed_to_update_profile_picture2)
                                }
                            } else {
                                uploadError = "Failed to get idToken"
                            }
                            isUploading = false
                        }
                    }.addOnFailureListener { e ->
                        uploadError =
                            context.getString(R.string.failed_to_get_download_url2, e.message)
                        isUploading = false
                    }
                }
                .addOnFailureListener { e ->
                    uploadError = context.getString(R.string.failed_to_upload_image2, e.message)
                    isUploading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        val idToken = FirebaseAuthHelper.getIdToken()
        if (idToken != null) {
            val consultants = ApiService.getAllConsultants(idToken)
            consultantData = consultants.find { it.consultant_uid == FirebaseAuthHelper.getUid() }
            if (consultantData != null) {
                nickname = consultantData!!.nickname
                description = consultantData!!.profile_description
                maxClients = consultantData!!.max_clients
                experienceYears = consultantData!!.experience_years
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF2F4F4F),
                modifier = Modifier.height(102.dp)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onHomeClick() },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "Home",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.home),
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
                    onClick = { onSearchClick() },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search",
                            modifier = Modifier.size(35.dp),
                            tint = Color.White
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.search),
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
                            text = stringResource(R.string.profile2),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF64A79B))
                .padding(top = 75.dp, bottom = padding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                        .shadow(8.dp, CircleShape)
                        .clickable {
                            pickImageLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                        )
                    } else if (consultantData?.profile_picture?.isNotEmpty() == true) {
                        AsyncImage(
                            model = consultantData!!.profile_picture,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(110.dp),
                            tint = Color.White
                        )
                    }

                    if (profileImageUri == null && (consultantData?.profile_picture?.isEmpty() != false)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.click_to_change_photo2),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (uploadError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = uploadError ?: "Unknown error",
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (successMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Green.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.nickname2),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F4F4F)
                    )
                ) {
                    TextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_your_nickname2),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White
                        )
                    )
                }

                Text(
                    text = stringResource(R.string.bio3),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F4F4F)
                    )
                ) {
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_your_bio2),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        },
                        singleLine = false,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White
                        )
                    )
                }

                Text(
                    text = stringResource(R.string.max_clients),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F4F4F)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { if (maxClients > 0) maxClients -= 1 },
                            modifier = Modifier.size(60.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_minus),
                                contentDescription = "Decrease max clients",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = "$maxClients",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(
                            onClick = { maxClients += 1 },
                            modifier = Modifier.size(60.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "Increase max clients",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.experience_years5),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F4F4F)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { if (experienceYears > 0) experienceYears -= 1 },
                            modifier = Modifier.size(60.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_minus),
                                contentDescription = "Decrease experience years",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = "$experienceYears",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(
                            onClick = { experienceYears += 1 },
                            modifier = Modifier.size(60.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "Increase experience years",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val idToken = FirebaseAuthHelper.getIdToken()
                            if (idToken != null) {
                                var success = true

                                if (nickname != consultantData?.nickname) {
                                    val nicknameSuccess =
                                        ApiService.updateConsultantNickname(idToken, nickname)
                                    if (!nicknameSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_nickname2)
                                    }
                                }

                                if (description != consultantData?.profile_description) {
                                    val descriptionSuccess =
                                        ApiService.updateConsultantProfileDescription(
                                            idToken,
                                            description
                                        )
                                    if (!descriptionSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_profile_description2)
                                    }
                                }

                                if (maxClients != consultantData?.max_clients) {
                                    val maxClientsSuccess =
                                        ApiService.updateConsultantMaxClients(idToken, maxClients)
                                    if (!maxClientsSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_max_clients2)
                                    }
                                }

                                if (experienceYears != consultantData?.experience_years) {
                                    val experienceYearsSuccess =
                                        ApiService.updateConsultantExperienceYears(
                                            idToken,
                                            experienceYears
                                        )
                                    if (!experienceYearsSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_experience_years)
                                    }
                                }

                                if (success) {
                                    val idToken = FirebaseAuthHelper.getIdToken()
                                    if (idToken != null) {
                                        val consultants = ApiService.getAllConsultants(idToken)
                                        consultantData =
                                            consultants.find { it.consultant_uid == FirebaseAuthHelper.getUid() }
                                        successMessage =
                                            context.getString(R.string.profile_updated_successfully2)
                                    }
                                }
                            } else {
                                uploadError = "Failed to get idToken"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F4F4F),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.save_changes2),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}