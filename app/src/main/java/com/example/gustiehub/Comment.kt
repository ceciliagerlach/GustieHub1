package com.example.gustiehub

data class Comment (
    val commentId: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
)