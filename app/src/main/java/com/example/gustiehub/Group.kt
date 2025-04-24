package com.example.gustiehub

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Group data class with name, creatorId, members, and description attributes
data class Group(
    var name: String = "",
    private var creatorId: String = "",
    var members: MutableList<String> = mutableListOf(),
    var description: String = "") {

    private val db= FirebaseFirestore.getInstance()


    // Function to create a new group in firestore
    fun createGroup() {
        val groupData = hashMapOf(
            "name" to name,
            "creatorId" to creatorId,
            "members" to members,
            "description" to description
        )

        db.collection("groups").document(name)
            .set(groupData)
            .addOnSuccessListener { println("Group created: $name") }
            .addOnFailureListener { e -> println("Error creating group: ${e.message}") }

        // add group to local dictionary
        GlobalData.groupDict.put(name, this)

    }

}