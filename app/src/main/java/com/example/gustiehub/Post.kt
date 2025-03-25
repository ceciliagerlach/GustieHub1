package com.example.gustiehub

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Post(
    val creatorId: String = "",
    val group: String = "",
    val text: String = "",
    val comments: List<Map<String, Any>> = emptyList(),
    val commentsEnabled: Boolean = true


    // don't know if we need this yet
//    @ServerTimestamp
//    val timestamp: Timestamp? = null // Firestore auto-generates this on creation
)
