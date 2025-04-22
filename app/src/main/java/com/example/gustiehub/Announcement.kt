package com.example.gustiehub

import com.google.firebase.Timestamp

// Announcement data class with header, text, and timestamp attributes
data class Announcement(
    val header: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)