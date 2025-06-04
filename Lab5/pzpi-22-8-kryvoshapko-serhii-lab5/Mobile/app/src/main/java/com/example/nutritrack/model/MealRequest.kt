package com.example.nutritrack.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val idToken: String)

@Serializable
data class AddMealRequest(
    val idToken: String,
    val meal_type: String,
    val products: List<ProductData>
)

@Serializable
data class ProductData(
    val product_name: String,
    val quantity_grams: Int,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float
)

@Serializable
data class MealEntry(
    val entry_id: Int,
    val meal_type: String,
    val entry_date: String,
    val product_name: String,
    val quantity_grams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val created_at: String
)
