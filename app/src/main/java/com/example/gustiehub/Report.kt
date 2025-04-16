package com.example.gustiehub

data class Report(
    val reporterId: String,
    val contentType: String,  // comment, post, item
    val contentId: String,
    val reason: String,
    val timestamp: com.google.firebase.Timestamp? = null
)