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
    val comments: List<Map<String, Any>> = emptyList(),
    val commentsEnabled: Boolean = true,
    @ServerTimestamp val timestamp: Timestamp? = null
)

// don't know if we need this yet
//    @ServerTimestamp
//    val timestamp: Timestamp? = null // Firestore auto-generates this on creation

{
    data class Comment(
        val commentId: String = "",
        val userId: String = "",
        val text: String = "",
        @ServerTimestamp val timestamp: Timestamp? = null
    )
}
