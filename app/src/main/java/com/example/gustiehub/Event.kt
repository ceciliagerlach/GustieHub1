package com.example.gustiehub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

// Event data class with creatorId, eventName, group, text, time, location, and date attributes
data class Event(
    val creatorId: String = "" ,
    val eventName: String = "",
    val group: String = "",
    val text: String = "",
    val time: String = "",
    val location: String="",
    val date: String = "",
)  {

    private val db = FirebaseFirestore.getInstance()

    // Function to create event
    fun createEvent() {
        val eventData = hashMapOf(
            "eventName" to eventName,
            "creatorId" to creatorId,
            "group" to group,
            "text" to text,
            "time" to time,
            "location" to location,
            "date" to date
        )


        db.collection("events").document(eventName)
            .set(eventData)
            .addOnSuccessListener { println("Event created: $eventName") }
            .addOnFailureListener { e -> println("Error creating event: ${e.message}") }
    }
}