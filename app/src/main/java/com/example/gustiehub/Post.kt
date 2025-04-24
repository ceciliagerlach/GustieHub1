package com.example.gustiehub

// Post data class with postId, creatorId, creatorName, group, text, comments, commentsEnabled, and timestamp attributes
data class Post(
    val postId: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val group: String = "",
    val text: String = "",
    val comments: List<Comment> = emptyList(),
    val commentsEnabled: Boolean = true,
    val timestamp: com.google.firebase.Timestamp? = null,
)
