package com.example.gustiehub

import com.google.firebase.Timestamp

data class Conversation(
    val lastMessage: String = "",
    val lastUpdated: Timestamp = Timestamp.now(),
    val nameOfUser: String = "",
    val userIds: List<String> = emptyList()
)