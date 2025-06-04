package com.example.nutritrack.screens.user

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
import androidx.compose.foundation.layout.width
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
import com.example.nutritrack.model.UserData
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    onSuccessScreenClick: () -> Unit,
    onNotebookClick: () -> Unit,
    onConsultantsClick: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf(0) }
    var userData by remember { mutableStateOf<UserData?>(null) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showGoalAchieved by remember { mutableStateOf(false) }
    var targetWeight by remember { mutableStateOf(0) }

    // Create a scroll state for the column
    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
                                val success =
                                    ApiService.updateProfilePicture(idToken, downloadUri.toString())
                                if (success) {
                                    val uid = FirebaseAuthHelper.getUid()
                                    if (uid != null) {
                                        userData = ApiService.getUserByUid(uid)
                                        profileImageUri = null
                                    }
                                } else {
                                    uploadError =
                                        context.getString(R.string.failed_to_update_profile_picture)
                                }
                            } else {
                                uploadError = context.getString(R.string.failed_to_get_idtoken)
                            }
                            isUploading = false
                        }
                    }.addOnFailureListener { e ->
                        uploadError =
                            context.getString(R.string.failed_to_get_download_url, e.message)
                        isUploading = false
                    }
                }
                .addOnFailureListener { e ->
                    uploadError = context.getString(R.string.failed_to_upload_image, e.message)
                    isUploading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuthHelper.getUid()
        if (uid != null) {
            userData = ApiService.getUserByUid(uid)
            if (userData != null) {
                nickname = userData!!.nickname
                description = userData!!.profile_description
                weight = userData!!.current_weight
            }
            val idToken = FirebaseAuthHelper.getIdToken()
            if (idToken != null) {
                val goalIds = ApiService.getAllUserGoalIds(idToken)
                if (goalIds.isNotEmpty()) {
                    val goalId = goalIds.first().goalId
                    val goal = ApiService.getSpecificGoalById(goalId)
                    if (goal != null) {
                        targetWeight = goal.targetWeight
                    }
                }
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
                    selected = false,
                    onClick = { onConsultantsClick() },
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
                    selected = true,
                    onClick = { /* Already on profile screen */ },
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF64A79B))
                .padding(top = 75.dp)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
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
                    } else if (userData?.profile_picture?.isNotEmpty() == true) {
                        AsyncImage(
                            model = userData!!.profile_picture,
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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.click_to_change_photo),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                if (uploadError != null) {
                    Text(
                        text = uploadError ?: "Unknown error",
                        fontSize = 16.sp,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                if (successMessage != null) {
                    Text(
                        text = successMessage ?: "",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Text(
                    text = stringResource(R.string.nickname),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
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
                                text = stringResource(R.string.enter_your_nickname),
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
                    text = stringResource(R.string.bio),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
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
                                text = stringResource(R.string.enter_your_bio),
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
                    text = stringResource(R.string.target_weight_kg, targetWeight),
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
                        .height(80.dp),
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
                            onClick = { if (weight > 0) weight -= 1 },
                            modifier = Modifier
                                .size(80.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_minus),
                                contentDescription = "Decrease weight",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = stringResource(R.string.kg3, weight),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { weight += 1 },
                            modifier = Modifier
                                .size(80.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "Increase weight",
                                tint = Color.White
                            )
                        }
                    }

                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val idToken = FirebaseAuthHelper.getIdToken()
                            if (idToken != null) {
                                var success = true

                                if (nickname != userData?.nickname) {
                                    val nicknameSuccess =
                                        ApiService.updateNickname(idToken, nickname)
                                    if (!nicknameSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_nickname)
                                    }
                                }

                                if (description != userData?.profile_description) {
                                    val descriptionSuccess =
                                        ApiService.updateProfileDescription(idToken, description)
                                    if (!descriptionSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_profile_description)
                                    }
                                }

                                if (weight != userData?.current_weight) {
                                    val weightSuccess =
                                        ApiService.updateCurrentWeight(idToken, weight)
                                    if (!weightSuccess) {
                                        success = false
                                        uploadError =
                                            context.getString(R.string.failed_to_update_weight)
                                    } else {
                                        val goalIds = ApiService.getAllUserGoalIds(idToken)
                                        if (goalIds.isNotEmpty()) {
                                            val goalId = goalIds.first().goalId
                                            val goal = ApiService.getSpecificGoalById(goalId)
                                            if (goal != null) {
                                                val targetWeight = goal.targetWeight
                                                val weightRange =
                                                    (targetWeight - 2)..(targetWeight + 2)
                                                if (weight in weightRange) {
                                                    showGoalAchieved = true
                                                }
                                            }
                                        }
                                    }
                                }

                                if (success) {
                                    val uid = FirebaseAuthHelper.getUid()
                                    if (uid != null) {
                                        userData = ApiService.getUserByUid(uid)
                                        successMessage =
                                            context.getString(R.string.profile_updated_successfully)
                                    }
                                }
                            } else {
                                uploadError = "Failed to get idToken"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F4F4F)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.save_changes),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Add additional spacer at the bottom to ensure there's space after the button
                // when scrolling to the bottom
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showGoalAchieved) {
            LaunchedEffect(showGoalAchieved) {
                val idToken = FirebaseAuthHelper.getIdToken()
                if (idToken != null) {
                    val goalIds = ApiService.getAllUserGoalIds(idToken)
                    if (goalIds.isNotEmpty()) {
                        val goalId = goalIds.first().goalId
                        val deleteSuccess = ApiService.deleteGoal(goalId, idToken)
                        if (!deleteSuccess) {
                            uploadError = "Failed to delete goal"
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F4F4F)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.congratulations),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.you_have_reached_the_weight_you_want_your_goal_will_be_deleted_create_a_new_one),
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                onSuccessScreenClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF64A79B)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.create_new_goal),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}