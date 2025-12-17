package com.example.kotlinquizzes.auth.model

data class UserData(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val profilePictureUrl: String?,
)