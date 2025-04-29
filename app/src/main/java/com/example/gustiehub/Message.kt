package com.example.gustiehub

import com.google.firebase.Timestamp

// Message data class with senderId, text, and timestamp attributes
data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
