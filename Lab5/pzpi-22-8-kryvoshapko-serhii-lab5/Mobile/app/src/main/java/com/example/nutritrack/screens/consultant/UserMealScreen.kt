package com.example.nutritrack.screens.consultant

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R
import com.example.nutritrack.data.api.ApiService
import com.example.nutritrack.data.auth.FirebaseAuthHelper
import com.example.nutritrack.model.ConsultantNote
import com.example.nutritrack.model.GoalResponse
import com.example.nutritrack.model.MealEntry
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserMealScreen(
    userUid: String,
    onBackClick: () -> Unit
) {
    var meals by remember { mutableStateOf<List<MealEntry>>(emptyList()) }
    var groupedMeals by remember {
        mutableStateOf<Map<Pair<String, String>, List<MealEntry>>>(
            emptyMap()
        )
    }
    var goal by remember { mutableStateOf<GoalResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteContent by remember { mutableStateOf("") }
    var noteError by remember { mutableStateOf<String?>(null) }
    var isSavingNote by remember { mutableStateOf(false) }
    var showNotesListDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf<List<ConsultantNote>>(emptyList()) }
    var editingNote by remember { mutableStateOf<ConsultantNote?>(null) }
    var editedNoteContent by remember { mutableStateOf("") }
    var isLoadingNotes by remember { mutableStateOf(false) }

    val currentDate = LocalDate.now()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(userUid) {
        try {
            isLoading = true
            errorMessage = null

            val goalIdResponse = ApiService.getGoalIdByUserUid(userUid)
            if (goalIdResponse != null) {
                val goalResponse = ApiService.getSpecificGoalById(goalIdResponse.goalId)
                goal = goalResponse
            } else {
                errorMessage = context.getString(R.string.no_goal_found_for_this_user)
            }

            meals = ApiService.getMealsByUid(userUid)
            if (meals.isEmpty()) {
                errorMessage = if (errorMessage != null) {
                    context.getString(R.string.no_meal_data_available_for_this_user, errorMessage)
                } else {
                    context.getString(R.string.no_meal_data_available_for_this_user2)
                }
            } else {
                val filteredMeals = meals.filter {
                    val mealDate = LocalDate.parse(
                        it.entry_date.split("T")[0],
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    mealDate == currentDate
                }
                if (filteredMeals.isEmpty()) {
                    errorMessage = if (errorMessage != null) {
                        context.getString(R.string.no_meal_data_available_for_today, errorMessage)
                    } else {
                        context.getString(R.string.no_meal_data_available_for_today2)
                    }
                } else {
                    groupedMeals = filteredMeals.groupBy { Pair(it.entry_date, it.meal_type) }
                }
            }
        } catch (e: Exception) {
            errorMessage = context.getString(R.string.error_loading_data, e.message)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(showNotesListDialog) {
        if (showNotesListDialog && goal?.goalId != null) {
            isLoadingNotes = true
            val idToken = FirebaseAuthHelper.getIdToken()
            if (idToken != null) {
                val currentGoal = goal
                if (currentGoal != null) {
                    val allNotes = ApiService.getNotes(currentGoal.goalId, idToken)
                    val filteredNotes = allNotes.filter {
                        val noteDate = LocalDateTime.parse(
                            it.created_at,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                        noteDate.toLocalDate() == currentDate
                    }
                    notes = filteredNotes
                }
            }
            isLoadingNotes = false
        }
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = {
                showNoteDialog = false
                noteContent = ""
                editingNote = null
                editedNoteContent = ""
            },
            title = {
                Text(
                    text = if (editingNote == null) stringResource(R.string.leave_a_note) else stringResource(
                        R.string.edit_note
                    ),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = if (editingNote == null) noteContent else editedNoteContent,
                        onValueChange = {
                            if (editingNote == null) noteContent = it else editedNoteContent = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2F4F4F),
                            unfocusedContainerColor = Color(0xFF2F4F4F),
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.enter_your_note_here),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    )
                    if (noteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = noteError!!,
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
                Button(
                    onClick = {
                        scope.launch {
                            isSavingNote = true
                            noteError = null
                            val idToken = FirebaseAuthHelper.getIdToken()
                            val currentGoal = goal
                            if (idToken != null && currentGoal?.goalId != null) {
                                val success = if (editingNote == null) {
                                    ApiService.addNote(idToken, currentGoal.goalId, noteContent)
                                } else {
                                    ApiService.updateNote(
                                        idToken,
                                        editingNote!!.note_id,
                                        editedNoteContent
                                    )
                                }
                                if (success) {
                                    val allNotes = ApiService.getNotes(currentGoal.goalId, idToken)
                                    notes = allNotes.filter {
                                        val noteDate = LocalDateTime.parse(
                                            it.created_at,
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                        )
                                        noteDate.toLocalDate() == currentDate
                                    }
                                    noteContent = ""
                                    editedNoteContent = ""
                                    editingNote = null
                                    showNoteDialog = false
                                } else {
                                    noteError =
                                        if (editingNote == null) context.getString(R.string.failed_to_save_note) else context.getString(
                                            R.string.failed_to_update_note
                                        )
                                }
                            } else {
                                noteError =
                                    context.getString(R.string.authentication_or_goal_id_error)
                            }
                            isSavingNote = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64A79B),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF64A79B).copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    enabled = !isSavingNote && (if (editingNote == null) noteContent.isNotEmpty() else editedNoteContent.isNotEmpty()) && goal != null,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSavingNote) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (editingNote == null) stringResource(R.string.send) else stringResource(
                                R.string.update
                            ),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNoteDialog = false
                        noteContent = ""
                        editingNote = null
                        editedNoteContent = ""
                        noteError = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.cancel), fontSize = 16.sp)
                }
            }
        )
    }

    if (showNotesListDialog) {
        AlertDialog(
            onDismissRequest = { showNotesListDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.today_s_notes),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                if (isLoadingNotes) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (notes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_notes_for_today),
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notes) { note ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F4F4F))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = note.content,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.created,
                                            formatTime(note.created_at)
                                        ),
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                editingNote = note
                                                editedNoteContent = note.content
                                                showNoteDialog = true
                                                showNotesListDialog = false
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                        ) {
                                            Text(stringResource(R.string.edit), fontSize = 14.sp)
                                        }
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    val idToken = FirebaseAuthHelper.getIdToken()
                                                    val currentGoal =
                                                        goal // Capture the current value of goal
                                                    if (idToken != null && currentGoal != null) {
                                                        val success = ApiService.deleteNote(
                                                            idToken,
                                                            note.note_id
                                                        )
                                                        if (success) {
                                                            val allNotes = ApiService.getNotes(
                                                                currentGoal.goalId,
                                                                idToken
                                                            )
                                                            notes = allNotes.filter {
                                                                val noteDate = LocalDateTime.parse(
                                                                    it.created_at,
                                                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                                                )
                                                                noteDate.toLocalDate() == currentDate
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                        ) {
                                            Text(stringResource(R.string.delete), fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF2F4F4F),
            shape = RoundedCornerShape(12.dp),
            confirmButton = {
                TextButton(
                    onClick = { showNotesListDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.close), fontSize = 16.sp)
                }
            },
            dismissButton = {}
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64A79B))
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.meal_history),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 10.dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        modifier = Modifier.size(35.dp),
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        showNotesListDialog = true
                    },
                    enabled = goal != null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_messages),
                        contentDescription = "Messages",
                        modifier = Modifier.size(35.dp),
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2F4F4F)
            ),
            modifier = Modifier.height(75.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            if (goal != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2F4F4F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.user_s_daily_goal),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.calories_kcal, goal!!.dailyCalories),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.protein_g, goal!!.dailyProtein),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.carbs_g, goal!!.dailyCarbs),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = stringResource(R.string.fats_g, goal!!.dailyFats),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (groupedMeals.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(groupedMeals.entries.toList()) { (key, mealGroup) ->
                            val (date, mealType) = key
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F4F4F))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.meal_type,
                                            mealType.replaceFirstChar { it.uppercase() }),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(R.string.date2, formatDate(date)),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.created2,
                                            formatTime(mealGroup.first().created_at)
                                        ),
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                    mealGroup.forEach { meal ->
                                        Text(
                                            text = stringResource(
                                                R.string.g5,
                                                meal.product_name,
                                                meal.quantity_grams
                                            ),
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                    val totalCalories = mealGroup.sumOf { it.calories }
                                    val totalProtein = mealGroup.sumOf { it.protein }
                                    val totalCarbs = mealGroup.sumOf { it.carbs }
                                    val totalFats = mealGroup.sumOf { it.fats }
                                    Text(
                                        text = stringResource(
                                            R.string.total_calories_kcal,
                                            totalCalories
                                        ),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.total_protein_g,
                                            totalProtein
                                        ),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = stringResource(R.string.total_carbs_g, totalCarbs),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = stringResource(R.string.total_fats_g, totalFats),
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showNoteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F4F)),
                shape = RoundedCornerShape(8.dp),
                enabled = goal != null
            ) {
                Text(
                    text = stringResource(R.string.leave_a_note2),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(dateStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(dateStr, formatter)
        DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dateTime)
    } catch (e: Exception) {
        dateStr
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(timeStr: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateTime = LocalDateTime.parse(timeStr, formatter)
        DateTimeFormatter.ofPattern("HH:mm").format(dateTime)
    } catch (e: Exception) {
        timeStr
    }
}