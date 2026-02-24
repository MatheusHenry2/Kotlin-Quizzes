package com.example.kotlinquizzes.feature.auth.data.model

data class UserData(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val profilePictureUrl: String?,
)