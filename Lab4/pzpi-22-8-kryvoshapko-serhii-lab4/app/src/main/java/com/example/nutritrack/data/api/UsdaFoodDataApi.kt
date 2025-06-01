package com.example.nutritrack.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class UsdaFoodDataApi(private val apiKey: String) {
    private val baseUrl = "https://api.nal.usda.gov/fdc/v1"
    private val tag = "UsdaFoodDataApi"


    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Search for food items by query
     * @param query Search query
     * @param maxResults Maximum number of results
     * @return List of food items
     */
    suspend fun searchFood(query: String, maxResults: Int = 25): List<FoodItem> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Starting search for query: $query, maxResults: $maxResults")

                val url = "$baseUrl/foods/search?query=${encode(query)}" +
                        "&pageSize=$maxResults" +
                        "&dataType=Foundation,SR%20Legacy,Survey%20(FNDDS),Branded" +
                        "&sortBy=dataType.keyword" +
                        "&sortOrder=asc" +
                        "&api_key=$apiKey"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody.isNullOrBlank()) {
                        Log.e(tag, "Received empty response from API")
                        return@withContext emptyList()
                    }

                    Log.d(tag, "Successful response: ${responseBody.take(100)}...")
                    val result = parseFoodSearchResponse(responseBody)
                    Log.d(tag, "Processed ${result.size} items")
                    result
                } else {
                    Log.e(tag, "API error: ${response.code} - ${response.message}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error during food search: ${e.message}", e)
                emptyList()
            }
        }
    }

    /**
     * Retrieve detailed information about a specific food item
     * @param fdcId Food item identifier
     * @return Food item details or null if not found
     */
    suspend fun getFoodDetails(fdcId: String): FoodItem? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "Fetching details for fdcId: $fdcId")
                val url = "$baseUrl/food/$fdcId?api_key=$apiKey&format=full"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody.isNullOrBlank()) {
                        Log.e(tag, "Received empty response for fdcId: $fdcId")
                        return@withContext null
                    }

                    Log.d(tag, "Successful response for details: ${responseBody.take(100)}...")
                    parseFoodDetailsResponse(responseBody)
                } else {
                    Log.e(tag, "API error for details: ${response.code} - ${response.message}")
                    null
                }
            } catch (e: Exception) {
                Log.e(tag, "Error retrieving food details: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Parse JSON response from food search API
     * @param json JSON response
     * @return List of food items
     */
    private fun parseFoodSearchResponse(json: String): List<FoodItem> {
        val foodList = mutableListOf<FoodItem>()
        try {
            val jsonObject = JSONObject(json)
            if (!jsonObject.has("foods")) {
                Log.e(tag, "Field 'foods' not found in response")
                return emptyList()
            }

            val foodsArray = jsonObject.getJSONArray("foods")
            for (i in 0 until foodsArray.length()) {
                try {
                    val food = foodsArray.getJSONObject(i)
                    val fdcId = food.optString("fdcId", "")
                    if (fdcId.isBlank()) {
                        Log.w(tag, "Item without fdcId skipped")
                        continue
                    }

                    val description = cleanFoodDescription(food.optString("description", ""))
                    val brandName = food.optString("brandName", "").trim()
                    val dataType = food.optString("dataType", "")

                    val nameBuilder = StringBuilder()
                    nameBuilder.append(description)
                    if (brandName.isNotBlank() && !description.contains(
                            brandName,
                            ignoreCase = true
                        )
                    ) {
                        nameBuilder.append(" (").append(brandName).append(")")
                    }

                    val fullName = nameBuilder.toString()

                    if (fullName.isBlank()) {
                        Log.w(tag, "Item with empty name skipped, fdcId: $fdcId")
                        continue
                    }

                    val nutrients = extractNutrients(food)

                    val additionalDesc = food.optString("additionalDescriptions", "").trim()
                    val ingredients = food.optString("ingredients", "").trim()

                    val descBuilder = StringBuilder()
                    if (brandName.isNotBlank()) {
                        descBuilder.append("Brand: ").append(brandName)
                    }
                    if (dataType.isNotBlank()) {
                        if (descBuilder.isNotEmpty()) descBuilder.append(" • ")
                        descBuilder.append("Type: ").append(prettifyDataType(dataType))
                    }
                    if (ingredients.isNotBlank() && ingredients.length < 100) {
                        if (descBuilder.isNotEmpty()) descBuilder.append(" • ")
                        descBuilder.append("Ingredients: ").append(ingredients)
                    }
                    if (additionalDesc.isNotBlank() && additionalDesc.length < 100) {
                        if (descBuilder.isNotEmpty()) descBuilder.append(" • ")
                        descBuilder.append(additionalDesc)
                    }

                    val finalDescription = descBuilder.toString()

                    val foodItem = FoodItem(
                        id = fdcId,
                        name = fullName,
                        description = finalDescription,
                        calories = nutrients["calories"]?.toInt() ?: 0,
                        protein = nutrients["protein"] ?: 0f,
                        fat = nutrients["fat"] ?: 0f,
                        carbs = nutrients["carbs"] ?: 0f,
                        servingDescription = "100g"
                    )

                    Log.d(
                        tag,
                        "Item added: ${foodItem.name}, calories: ${foodItem.calories}, " +
                                "protein: ${foodItem.protein}, fat: ${foodItem.fat}, carbs: ${foodItem.carbs}"
                    )

                    foodList.add(foodItem)
                } catch (e: Exception) {
                    Log.e(tag, "Error processing item #$i: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing search response: ${e.message}", e)
        }
        return foodList
    }

    private fun prettifyDataType(dataType: String): String {
        return when (dataType) {
            "Branded" -> "Branded"
            "Foundation" -> "Foundation"
            "SR Legacy" -> "Standard"
            "Survey (FNDDS)" -> "Survey"
            else -> dataType
        }
    }


    private fun cleanFoodDescription(description: String): String {
        return description.trim()
            .replace(Regex("\\s+"), " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /**
     * Parse JSON response with food item details
     * @param json JSON response
     * @return FoodItem with detailed information or null on parsing error
     */
    private fun parseFoodDetailsResponse(json: String): FoodItem? {
        try {
            val food = JSONObject(json)
            val fdcId = food.optString("fdcId", "")

            val description = cleanFoodDescription(food.optString("description", ""))
            val brandName = food.optString("brandName", "").trim()
            val dataType = food.optString("dataType", "")

            val nameBuilder = StringBuilder()
            nameBuilder.append(description)
            if (brandName.isNotBlank() && !description.contains(brandName, ignoreCase = true)) {
                nameBuilder.append(" (").append(brandName).append(")")
            }

            val fullName = nameBuilder.toString()

            if (fullName.isBlank()) {
                Log.w(tag, "Item details have empty name, fdcId: $fdcId")
                return null
            }

            val ingredients = food.optString("ingredients", "").trim()
            val additionalDesc = food.optString("additionalDescriptions", "").trim()
            val servingSize = food.optJSONObject("servingSize")?.optDouble("value", 0.0) ?: 0.0
            val servingSizeUnit = food.optString("servingSizeUnit", "")

            val detailsBuilder = StringBuilder()
            if (brandName.isNotBlank()) {
                detailsBuilder.append("Brand: ").append(brandName).append("\n")
            }
            if (dataType.isNotBlank()) {
                detailsBuilder.append("Type: ").append(prettifyDataType(dataType)).append("\n")
            }
            if (ingredients.isNotBlank()) {
                detailsBuilder.append("Ingredients: ").append(ingredients).append("\n")
            }
            if (servingSize > 0 && servingSizeUnit.isNotBlank()) {
                detailsBuilder.append("Serving Size: ").append(servingSize).append(" ")
                    .append(servingSizeUnit).append("\n")
            }
            if (additionalDesc.isNotBlank()) {
                detailsBuilder.append(additionalDesc)
            }

            val detailedDescription = detailsBuilder.toString().trim()

            val nutrients = extractDetailedNutrients(food)

            val foodItem = FoodItem(
                id = fdcId,
                name = fullName,
                description = detailedDescription,
                calories = nutrients["calories"]?.toInt() ?: 0,
                protein = nutrients["protein"] ?: 0f,
                fat = nutrients["fat"] ?: 0f,
                carbs = nutrients["carbs"] ?: 0f,
                servingDescription = "100g"
            )

            Log.d(
                tag,
                "Received item details: ${foodItem.name}, calories: ${foodItem.calories}, " +
                        "protein: ${foodItem.protein}, fat: ${foodItem.fat}, carbs: ${foodItem.carbs}"
            )

            return foodItem
        } catch (e: Exception) {
            Log.e(tag, "Error processing item details: ${e.message}", e)
            return null
        }
    }

    /**
     * Extract nutrients from food JSON object (for search)
     * @param food JSON object with food data
     * @return Map of nutrients
     */
    private fun extractNutrients(food: JSONObject): Map<String, Float> {
        val nutrients = mutableMapOf<String, Float>()
        try {
            nutrients["calories"] = 0f
            nutrients["protein"] = 0f
            nutrients["fat"] = 0f
            nutrients["carbs"] = 0f

            if (food.has("foodNutrients")) {
                val nutrientArray = food.getJSONArray("foodNutrients")
                for (i in 0 until nutrientArray.length()) {
                    val nutrient = nutrientArray.getJSONObject(i)

                    var nutrientId = 0
                    var amount = 0f

                    if (nutrient.has("nutrientId")) {
                        nutrientId = nutrient.optInt("nutrientId", 0)
                        amount = nutrient.optDouble("value", 0.0).toFloat()
                    } else if (nutrient.has("nutrient")) {
                        val nutrientObj = nutrient.getJSONObject("nutrient")
                        nutrientId = nutrientObj.optInt("id", 0)
                        amount = nutrient.optDouble("amount", 0.0).toFloat()
                    }

                    when (nutrientId) {
                        1008 -> nutrients["calories"] = amount
                        1003 -> nutrients["protein"] = amount
                        1004 -> nutrients["fat"] = amount
                        1005 -> nutrients["carbs"] = amount
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error extracting nutrients: ${e.message}", e)
        }
        return nutrients
    }

    /**
     * Extract detailed nutrients (for detailed view of food item)
     * @param food JSON object with food data
     * @return Map of nutrients
     */
    private fun extractDetailedNutrients(food: JSONObject): Map<String, Float> {
        val nutrients = mutableMapOf<String, Float>()
        try {
            nutrients["calories"] = 0f
            nutrients["protein"] = 0f
            nutrients["fat"] = 0f
            nutrients["carbs"] = 0f

            if (food.has("foodNutrients")) {
                val nutrientArray = food.getJSONArray("foodNutrients")
                for (i in 0 until nutrientArray.length()) {
                    val nutrient = nutrientArray.getJSONObject(i)

                    var nutrientId = 0
                    var amount = 0f
                    var name = ""

                    if (nutrient.has("nutrient")) {
                        val nutrientObj = nutrient.getJSONObject("nutrient")
                        nutrientId = nutrientObj.optInt("id", 0)
                        name = nutrientObj.optString("name", "")
                        amount = nutrient.optDouble("amount", 0.0).toFloat()
                    } else {
                        nutrientId = nutrient.optInt("nutrientId", 0)
                        name = nutrient.optString("nutrientName", "")
                        amount = nutrient.optDouble("value", 0.0).toFloat()
                    }

                    when {
                        nutrientId == 1008 || name.contains("Energy", ignoreCase = true) ->
                            nutrients["calories"] = amount

                        nutrientId == 1003 || name.contains("Protein", ignoreCase = true) ->
                            nutrients["protein"] = amount

                        nutrientId == 1004 || name.contains("Total lipid", ignoreCase = true) ->
                            nutrients["fat"] = amount

                        nutrientId == 1005 || name.contains("Carbohydrate", ignoreCase = true) ->
                            nutrients["carbs"] = amount
                    }
                }
            }

            if (food.has("labelNutrients")) {
                extractFromLabelNutrients(food.getJSONObject("labelNutrients"), nutrients)
            }

            Log.d(
                tag,
                "Extracted nutrients (per 100g): calories=${nutrients["calories"]}, " +
                        "protein=${nutrients["protein"]}, fat=${nutrients["fat"]}, carbs=${nutrients["carbs"]}"
            )
        } catch (e: Exception) {
            Log.e(tag, "Error extracting detailed nutrients: ${e.message}", e)
        }
        return nutrients
    }

    private fun extractFromLabelNutrients(
        labelNutrients: JSONObject,
        nutrients: MutableMap<String, Float>
    ) {
        try {
            if (labelNutrients.has("calories") && nutrients["calories"] == 0f) {
                val caloriesObj = labelNutrients.getJSONObject("calories")
                nutrients["calories"] = caloriesObj.optDouble("value", 0.0).toFloat()
            }

            if (labelNutrients.has("protein") && nutrients["protein"] == 0f) {
                val proteinObj = labelNutrients.getJSONObject("protein")
                nutrients["protein"] = proteinObj.optDouble("value", 0.0).toFloat()
            }

            if (labelNutrients.has("fat") && nutrients["fat"] == 0f) {
                val fatObj = labelNutrients.getJSONObject("fat")
                nutrients["fat"] = fatObj.optDouble("value", 0.0).toFloat()
            }

            if (labelNutrients.has("carbohydrates") && nutrients["carbs"] == 0f) {
                val carbsObj = labelNutrients.getJSONObject("carbohydrates")
                nutrients["carbs"] = carbsObj.optDouble("value", 0.0).toFloat()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error extracting nutrients from label: ${e.message}")
        }
    }

    /**
     * URL encode a string
     * @param value String to encode
     * @return Encoded string
     */
    private fun encode(value: String): String {
        return java.net.URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
    }
}

@Serializable
data class FoodItem(
    val id: String = "",
    val name: String,
    val description: String = "",
    val calories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val servingDescription: String = "100g"
) {
    /**
     * Calculate nutrients for a specific weight
     * @param weightGrams Weight in grams
     * @return New FoodItem with recalculated nutrients
     */
    fun calculateNutrientsForWeight(weightGrams: Float): FoodItem {
        val servingWeight = servingDescription
            .replace("[^0-9.]".toRegex(), "")
            .toFloatOrNull() ?: 100f

        val ratio = weightGrams / servingWeight

        return FoodItem(
            id = id,
            name = name,
            description = description,
            calories = (calories * ratio).toInt(),
            protein = protein * ratio,
            fat = fat * ratio,
            carbs = carbs * ratio,
            servingDescription = "${weightGrams}g"
        )
    }
}