package com.example.gustiehub

import com.google.firebase.Timestamp

data class Announcement(
    val header: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)