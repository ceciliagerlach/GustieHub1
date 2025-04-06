package com.example.gustiehub

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp

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
