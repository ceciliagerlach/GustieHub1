package com.example.gustiehub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Post (
    val creatorId: String = "",
    val group: String = "",
    val text: String = "",
    val comments: List<Map<String, Any>> = emptyList()
)

