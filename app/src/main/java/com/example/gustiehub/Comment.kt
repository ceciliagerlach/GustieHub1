package com.example.gustiehub

// Comment data class with commentId, userId, text, and timestamp attributes
data class Comment (
    val commentId: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
)