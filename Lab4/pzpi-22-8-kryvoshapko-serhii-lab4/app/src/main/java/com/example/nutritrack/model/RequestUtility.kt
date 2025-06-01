package com.example.nutritrack.util

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class RequestMetadata(
    val requestId: Int,
    val initiator: String // "user" або "consultant"
)

object LocalStorageUtil {
    private const val FILE_NAME = "requests_metadata.json"

    fun saveRequestMetadata(context: Context, metadata: RequestMetadata) {
        val file = File(context.filesDir, FILE_NAME)
        val currentData = if (file.exists()) {
            readRequestMetadata(context).toMutableList()
        } else {
            mutableListOf()
        }

        // Перевіряємо, чи вже є запис із таким requestId
        val existingIndex = currentData.indexOfFirst { it.requestId == metadata.requestId }
        if (existingIndex != -1) {
            currentData[existingIndex] = metadata
        } else {
            currentData.add(metadata)
        }

        val jsonString = Json.encodeToString(currentData)
        file.writeText(jsonString)
    }

    fun readRequestMetadata(context: Context): List<RequestMetadata> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        val jsonString = file.readText()
        return Json.decodeFromString(jsonString)
    }

    fun removeRequestMetadata(context: Context, requestId: Int) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return

        val currentData = readRequestMetadata(context).toMutableList()
        currentData.removeAll { it.requestId == requestId }

        val jsonString = Json.encodeToString(currentData)
        file.writeText(jsonString)
    }
}