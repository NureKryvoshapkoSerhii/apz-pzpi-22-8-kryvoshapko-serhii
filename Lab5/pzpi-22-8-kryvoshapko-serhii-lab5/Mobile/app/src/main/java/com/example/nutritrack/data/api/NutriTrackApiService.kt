package com.example.nutritrack.data.api

import android.util.Log
import com.example.nutritrack.data.user.UpdateUserCurrentWeightRequest
import com.example.nutritrack.data.user.UpdateUserNicknameRequest
import com.example.nutritrack.data.user.UpdateUserProfileDescriptionRequest
import com.example.nutritrack.data.user.UpdateUserProfilePictureRequest
import com.example.nutritrack.model.AddMealRequest
import com.example.nutritrack.model.AddNoteRequest
import com.example.nutritrack.model.AddStreakRequest
import com.example.nutritrack.model.Consultant
import com.example.nutritrack.model.ConsultantNote
import com.example.nutritrack.model.ConsultantRegistrationData
import com.example.nutritrack.model.ConsultantRequest
import com.example.nutritrack.model.ConsultantRespondInviteRequest
import com.example.nutritrack.model.ConsultantSendInviteRequest
import com.example.nutritrack.model.GoalIdResponse
import com.example.nutritrack.model.GoalResponse
import com.example.nutritrack.model.LinkedRelationship
import com.example.nutritrack.model.MealEntry
import com.example.nutritrack.model.StreakResponse
import com.example.nutritrack.model.UpdateConsultantExperienceYearsRequest
import com.example.nutritrack.model.UpdateConsultantMaxClientsRequest
import com.example.nutritrack.model.UpdateConsultantNicknameRequest
import com.example.nutritrack.model.UpdateConsultantProfileDescriptionRequest
import com.example.nutritrack.model.UpdateConsultantProfilePictureRequest
import com.example.nutritrack.model.UpdateNoteRequest
import com.example.nutritrack.model.UpdateStreakRequest
import com.example.nutritrack.model.UserData
import com.example.nutritrack.model.UserGoalData
import com.example.nutritrack.model.UserRegistrationData
import com.example.nutritrack.model.UserRespondInviteRequest
import com.example.nutritrack.model.UserSendInviteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class LoginRequest(val idToken: String)

interface ApiServiceInterface {
    @POST("api/Auth/login/consultant")
    suspend fun loginConsultant(@Body request: LoginRequest): Response<Void>

    @POST("api/Auth/register/consultant")
    suspend fun registerConsultant(@Body data: ConsultantRegistrationData): Response<Void>

    @POST("api/Auth/login/user")
    suspend fun loginUser(@Body request: LoginRequest): Response<Void>

    @POST("api/Auth/register/user")
    suspend fun registerUser(@Body data: UserRegistrationData): Response<Void>

    @POST("api/Goal/create-user-goal")
    suspend fun createUserGoal(@Body data: UserGoalData): Response<Void>

    @GET("api/User/get-user-by-uid")
    suspend fun getUserByUid(@Query("uid") uid: String): Response<UserData>

    @GET("api/Goal/get-all-user-goal-ids")
    suspend fun getAllUserGoalIds(@Query("idToken") idToken: String): Response<List<GoalIdResponse>>

    @GET("api/Goal/get-specific-goal-by-id/{goalId}")
    suspend fun getSpecificGoalById(@Path("goalId") goalId: Int): Response<GoalResponse>

    @PUT("api/User/update-profile-picture")
    suspend fun updateProfilePicture(@Body request: UpdateUserProfilePictureRequest): Response<Void>

    @PUT("api/User/update-nickname")
    suspend fun updateNickname(@Body request: UpdateUserNicknameRequest): Response<Void>

    @PUT("api/User/update-profile-description")
    suspend fun updateProfileDescription(@Body request: UpdateUserProfileDescriptionRequest): Response<Void>

    @PUT("api/User/update-current-weight")
    suspend fun updateCurrentWeight(@Body request: UpdateUserCurrentWeightRequest): Response<Void>

    @DELETE("api/Goal/delete-goal/{goalId}")
    suspend fun deleteGoal(
        @Path("goalId") goalId: Int,
        @Query("idToken") idToken: String
    ): Response<Void>

    @POST("api/Streak/add-streak")
    suspend fun addStreak(@Body request: AddStreakRequest): Response<Void>

    @GET("api/Streak/get-streaks")
    suspend fun getStreaks(@Query("idToken") idToken: String): Response<List<StreakResponse>>

    @PUT("api/Streak/update-streak")
    suspend fun updateStreak(@Body request: UpdateStreakRequest): Response<Void>

    @POST("api/Meal/add-meal")
    suspend fun addMeal(@Body request: AddMealRequest): Response<Void>

    @GET("api/Meal/get-all-meals")
    suspend fun getAllMeals(@Query("idToken") idToken: String): List<MealEntry>

    @DELETE("api/Meal/delete-meal")
    suspend fun deleteMeal(
        @Query("idToken") idToken: String,
        @Query("entryId") entryId: Int
    ): Response<Void>

    @GET("api/Consultant/get-all-consultants")
    suspend fun getAllConsultants(@Query("idToken") idToken: String): List<Consultant>

    @POST("api/Consultant/user-send-invite")
    suspend fun sendInviteToConsultant(@Body request: UserSendInviteRequest): Response<Void>

    @GET("api/Consultant/get-linked-relationships")
    suspend fun getLinkedRelationships(@Query("idToken") idToken: String): List<LinkedRelationship>

    @DELETE("api/User/remove-consultant")
    suspend fun removeConsultant(
        @Query("idToken") idToken: String,
        @Query("consultant_uid") consultantUid: String
    ): Response<Void>

    @GET("api/ConsultantNote/get-notes")
    suspend fun getNotes(
        @Query("goalId") goalId: Int,
        @Query("idToken") idToken: String
    ): List<ConsultantNote>

    @GET("api/User/get-all-users")
    suspend fun getAllUsers(@Query("idToken") idToken: String): List<UserData>

    @POST("api/Consultant/send-invite-to-user")
    suspend fun sendInviteToUser(@Body request: ConsultantSendInviteRequest): Response<Void>

    @DELETE("api/Consultant/consultant-remove-user")
    suspend fun removeUser(
        @Query("idToken") idToken: String,
        @Query("user_uid") userUid: String
    ): Response<Void>

    @GET("api/Consultant/get-all-requests")
    suspend fun getAllRequests(@Query("idToken") idToken: String): List<ConsultantRequest>

    @POST("api/Consultant/user-respond-invite")
    suspend fun userRespondInvite(@Body request: UserRespondInviteRequest): Response<Void>

    @GET("api/Meal/get-meals-by-uid")
    suspend fun getMealsByUid(@Query("uid") uid: String): List<MealEntry>

    @GET("api/Goal/get-goal-id-by-user-uid/{userUid}")
    suspend fun getGoalIdByUserUid(@Path("userUid") userUid: String): Response<GoalIdResponse>

    @POST("api/Consultant/consultant-respond-invite")
    suspend fun consultantRespondInvite(@Body request: ConsultantRespondInviteRequest): Response<Void>

    @POST("api/ConsultantNote/add-note")
    suspend fun addNote(@Body request: AddNoteRequest): Response<Void>

    @PUT("api/ConsultantNote/update-note")
    suspend fun updateNote(@Body request: UpdateNoteRequest): Response<Void>

    @DELETE("api/ConsultantNote/delete-note")
    suspend fun deleteNote(
        @Query("idToken") idToken: String,
        @Query("note_id") noteId: Int
    ): Response<Void>

    @PUT("api/Consultant/update-nickname")
    suspend fun updateConsultantNickname(@Body request: UpdateConsultantNicknameRequest): Response<Void>

    @PUT("api/Consultant/update-profile-picture")
    suspend fun updateConsultantProfilePicture(@Body request: UpdateConsultantProfilePictureRequest): Response<Void>

    @PUT("api/Consultant/update-profile-description")
    suspend fun updateConsultantProfileDescription(@Body request: UpdateConsultantProfileDescriptionRequest): Response<Void>

    @PUT("api/Consultant/update-max-clients")
    suspend fun updateConsultantMaxClients(@Body request: UpdateConsultantMaxClientsRequest): Response<Void>

    @PUT("api/Consultant/update-experience-years")
    suspend fun updateConsultantExperienceYears(@Body request: UpdateConsultantExperienceYearsRequest): Response<Void>
}

object ApiService {
    private val retrofit: Retrofit by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("http://192.168.0.183:5182")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: ApiServiceInterface by lazy {
        retrofit.create(ApiServiceInterface::class.java)
    }

    suspend fun checkConsultantExists(idToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.loginConsultant(LoginRequest(idToken))
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to check consultant: $e")
                false
            }
        }
    }

    suspend fun registerConsultant(data: ConsultantRegistrationData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.registerConsultant(data)
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to register consultant: $e")
                false
            }
        }
    }

    suspend fun checkUserExists(idToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.loginUser(LoginRequest(idToken))
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to check user: $e")
                false
            }
        }
    }

    suspend fun registerUser(data: UserRegistrationData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.registerUser(data)
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to register user: $e")
                false
            }
        }
    }

    suspend fun createUserGoal(data: UserGoalData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createUserGoal(data)
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to create user goal: $e")
                false
            }
        }
    }

    suspend fun getUserByUid(uid: String): UserData? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserByUid(uid)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("ApiService", "Failed to get user by UID: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get user by UID: $e")
                null
            }
        }
    }

    suspend fun getAllUserGoalIds(idToken: String): List<GoalIdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllUserGoalIds(idToken)
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    Log.e("ApiService", "Failed to get user goal IDs: ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get user goal IDs: $e")
                emptyList()
            }
        }
    }

    suspend fun getSpecificGoalById(goalId: Int): GoalResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSpecificGoalById(goalId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("ApiService", "Failed to get specific goal by ID: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get specific goal by ID: $e")
                null
            }
        }
    }

    suspend fun updateProfilePicture(idToken: String, profilePictureUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateUserProfilePictureRequest(idToken, profilePictureUrl)
                val response = apiService.updateProfilePicture(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to update profile picture: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update profile picture: $e")
                false
            }
        }
    }

    suspend fun updateNickname(idToken: String, newNickname: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateUserNicknameRequest(idToken, newNickname)
                val response = apiService.updateNickname(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to update nickname: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update nickname: $e")
                false
            }
        }
    }

    suspend fun updateProfileDescription(idToken: String, newDescription: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateUserProfileDescriptionRequest(idToken, newDescription)
                val response = apiService.updateProfileDescription(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to update profile description: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update profile description: $e")
                false
            }
        }
    }

    suspend fun updateCurrentWeight(idToken: String, newWeight: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateUserCurrentWeightRequest(idToken, newWeight)
                val response = apiService.updateCurrentWeight(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to update current weight: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update current weight: $e")
                false
            }
        }
    }

    suspend fun deleteGoal(goalId: Int, idToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteGoal(goalId, idToken)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to delete goal: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to delete goal: $e")
                false
            }
        }
    }

    suspend fun addStreak(idToken: String, currentStreak: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = AddStreakRequest(idToken, currentStreak)
                val response = apiService.addStreak(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to add streak: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to add streak: $e")
                false
            }
        }
    }

    suspend fun updateStreak(idToken: String, currentStreak: Int, isActive: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateStreakRequest(idToken, currentStreak, isActive)
                val response = apiService.updateStreak(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to update streak: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update streak: $e")
                false
            }
        }
    }

    suspend fun addMeal(request: AddMealRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addMeal(request)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Meal added successfully")
                    true
                } else {
                    Log.e("ApiService", "Failed to add meal: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to add meal: $e")
                false
            }
        }
    }

    suspend fun getAllMeals(idToken: String): List<MealEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllMeals(idToken)
                if (response.isNotEmpty()) {
                    Log.d("ApiService", "Meals retrieved successfully: ${response.size} entries")
                    response
                } else {
                    Log.w("ApiService", "No meals found")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get meals: $e")
                emptyList()
            }
        }
    }

    suspend fun deleteMeal(idToken: String, entryId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteMeal(idToken, entryId)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Meal deleted successfully: entryId=$entryId")
                    true
                } else {
                    Log.e("ApiService", "Failed to delete meal: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to delete meal: $e")
                false
            }
        }
    }

    suspend fun getAllConsultants(idToken: String): List<Consultant> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllConsultants(idToken)
                if (response.isNotEmpty()) {
                    Log.d(
                        "ApiService",
                        "Consultants retrieved successfully: ${response.size} entries"
                    )
                    response
                } else {
                    Log.w("ApiService", "No consultants found")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get consultants: $e")
                emptyList()
            }
        }
    }

    suspend fun sendInviteToConsultant(idToken: String, consultantUid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UserSendInviteRequest(idToken, consultantUid)
                val response = apiService.sendInviteToConsultant(request)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Invite sent successfully to consultant: $consultantUid")
                    true
                } else {
                    Log.e("ApiService", "Failed to send invite: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to send invite: $e")
                false
            }
        }
    }

    suspend fun sendInviteToUser(idToken: String, userUid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = ConsultantSendInviteRequest(idToken, userUid)
                val response = apiService.sendInviteToUser(request)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Invite sent successfully to user: $userUid")
                    true
                } else {
                    Log.e("ApiService", "Failed to send invite to user: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to send invite to user: $e")
                false
            }
        }
    }

    suspend fun removeConsultant(idToken: String, consultantUid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeConsultant(idToken, consultantUid)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Consultant removed successfully: $consultantUid")
                    true
                } else {
                    Log.e("ApiService", "Failed to remove consultant: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to remove consultant: $e")
                false
            }
        }
    }

    suspend fun getAllUsers(idToken: String): List<UserData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllUsers(idToken)
                if (response.isNotEmpty()) {
                    Log.d("ApiService", "Users retrieved successfully: ${response.size} entries")
                    response
                } else {
                    Log.w("ApiService", "No users found")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get users: $e")
                emptyList()
            }
        }
    }

    suspend fun getLinkedRelationships(idToken: String): List<LinkedRelationship> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLinkedRelationships(idToken)
                if (response.isNotEmpty()) {
                    Log.d(
                        "ApiService",
                        "Linked relationships retrieved successfully: ${response.size} entries"
                    )
                    response
                } else {
                    Log.w("ApiService", "No linked relationships found")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get linked relationships: $e")
                emptyList()
            }
        }
    }

    suspend fun getNotes(goalId: Int, idToken: String): List<ConsultantNote> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getNotes(goalId, idToken)
                if (response.isNotEmpty()) {
                    Log.d(
                        "ApiService",
                        "Notes retrieved successfully for goalId $goalId: ${response.size} entries"
                    )
                    response
                } else {
                    Log.w("ApiService", "No notes found for goalId $goalId")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get notes for goalId $goalId: $e")
                emptyList()
            }
        }
    }


    suspend fun removeUser(idToken: String, userUid: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeUser(idToken, userUid)
                if (response.isSuccessful) {
                    Log.d("ApiService", "User removed successfully: $userUid")
                    true
                } else {
                    Log.e("ApiService", "Failed to remove user: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to remove user: $e")
                false
            }
        }
    }

    suspend fun getAllRequests(idToken: String): List<ConsultantRequest> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllRequests(idToken)
                if (response.isNotEmpty()) {
                    Log.d("ApiService", "Requests retrieved successfully: ${response.size} entries")
                    response
                } else {
                    Log.w("ApiService", "No requests found")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get requests: $e")
                emptyList()
            }
        }
    }

    suspend fun getMealsByUid(uid: String): List<MealEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMealsByUid(uid)
                if (response.isNotEmpty()) {
                    Log.d(
                        "ApiService",
                        "Meals retrieved successfully for uid $uid: ${response.size} entries"
                    )
                    response
                } else {
                    Log.w("ApiService", "No meals found for uid $uid")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get meals by uid $uid: $e")
                emptyList()
            }
        }
    }

    suspend fun userRespondInvite(
        idToken: String,
        consultantUid: String,
        isAccepted: Boolean
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UserRespondInviteRequest(idToken, consultantUid, isAccepted)
                val response = apiService.userRespondInvite(request)
                if (response.isSuccessful) {
                    Log.d(
                        "ApiService",
                        "Responded to invite successfully: consultantUid=$consultantUid, isAccepted=$isAccepted"
                    )
                    true
                } else {
                    Log.e("ApiService", "Failed to respond to invite: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to respond to invite: $e")
                false
            }
        }
    }

    suspend fun getMostRecentStreak(idToken: String): StreakResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStreaks(idToken)
                if (response.isSuccessful) {
                    response.body()?.maxByOrNull { it.streak_date }
                } else {
                    Log.e("ApiService", "Failed to get streaks: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get streaks: $e")
                null
            }
        }
    }


    suspend fun consultantRespondInvite(
        idToken: String,
        userUid: String,
        isAccepted: Boolean
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = ConsultantRespondInviteRequest(idToken, userUid, isAccepted)
                val response = apiService.consultantRespondInvite(request)
                if (response.isSuccessful) {
                    Log.d(
                        "ApiService",
                        "Responded to user invite successfully: userUid=$userUid, isAccepted=$isAccepted"
                    )
                    true
                } else {
                    Log.e("ApiService", "Failed to respond to user invite: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to respond to user invite: $e")
                false
            }
        }
    }

    suspend fun getGoalIdByUserUid(userUid: String): GoalIdResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getGoalIdByUserUid(userUid)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    Log.e("ApiService", "Failed to get goal ID by user UID: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to get goal ID by user UID: $e")
                null
            }
        }
    }

    suspend fun addNote(idToken: String, goalId: Int, content: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = AddNoteRequest(idToken, goalId, content)
                val response = apiService.addNote(request)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Note added successfully for goalId $goalId")
                    true
                } else {
                    Log.e("ApiService", "Failed to add note: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to add note: $e")
                false
            }
        }
    }

    suspend fun updateNote(idToken: String, noteId: Int, content: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateNoteRequest(idToken, noteId, content)
                val response = apiService.updateNote(request)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Note updated successfully: noteId=$noteId")
                    true
                } else {
                    Log.e("ApiService", "Failed to update note: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update note: $e")
                false
            }
        }
    }

    suspend fun deleteNote(idToken: String, noteId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteNote(idToken, noteId)
                if (response.isSuccessful) {
                    Log.d("ApiService", "Note deleted successfully: noteId=$noteId")
                    true
                } else {
                    Log.e("ApiService", "Failed to delete note: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to delete note: $e")
                false
            }
        }
    }

    suspend fun updateConsultantExperienceYears(idToken: String, newExperienceYears: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateConsultantExperienceYearsRequest(idToken, newExperienceYears)
                val response = apiService.updateConsultantExperienceYears(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e(
                        "ApiService",
                        "Failed to update consultant experience years: ${response.code()}"
                    )
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update consultant experience years: $e")
                false
            }
        }
    }

    suspend fun updateConsultantNickname(idToken: String, newNickname: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateConsultantNicknameRequest(idToken, newNickname)
                val response = apiService.updateConsultantNickname(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "Failed to update consultant nickname: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update consultant nickname: $e")
                false
            }
        }
    }

    suspend fun updateConsultantProfilePicture(
        idToken: String,
        profilePictureUrl: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateConsultantProfilePictureRequest(idToken, profilePictureUrl)
                val response = apiService.updateConsultantProfilePicture(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e(
                        "ApiService",
                        "Failed to update consultant profile picture: ${response.code()}"
                    )
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update consultant profile picture: $e")
                false
            }
        }
    }

    suspend fun updateConsultantProfileDescription(
        idToken: String,
        newDescription: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateConsultantProfileDescriptionRequest(idToken, newDescription)
                val response = apiService.updateConsultantProfileDescription(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e(
                        "ApiService",
                        "Failed to update consultant profile description: ${response.code()}"
                    )
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update consultant profile description: $e")
                false
            }
        }
    }

    suspend fun updateConsultantMaxClients(idToken: String, newMaxClients: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateConsultantMaxClientsRequest(idToken, newMaxClients)
                val response = apiService.updateConsultantMaxClients(request)
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e(
                        "ApiService",
                        "Failed to update consultant max clients: ${response.code()}"
                    )
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "Failed to update consultant max clients: $e")
                false
            }
        }
    }
}
