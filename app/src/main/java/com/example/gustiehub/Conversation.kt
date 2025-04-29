package com.example.gustiehub

import com.google.firebase.Timestamp

// Conversation data class with lastMessage, lastUpdated, nameOfUser, and userIds attributes
data class Conversation(
    val lastMessage: String = "",
    val lastUpdated: Timestamp = Timestamp.now(),
    val nameOfUser: String = "",
    val userIds: List<String> = emptyList()
)