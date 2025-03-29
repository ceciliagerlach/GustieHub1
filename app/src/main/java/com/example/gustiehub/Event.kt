package com.example.gustiehub

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp

data class Event(
    val creatorId: String = "",
    val eventName: String = "",
    val group: String = "",
    val text: String = "",
    val time: String = "",
    val location: String = "",
    val date: String = ""
)
