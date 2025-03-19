package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore

object GlobalData {
    var Gusties: Group? = null

    fun initializeGlobalData() {
        val db = FirebaseFirestore.getInstance()
        val groupsRef = db.collection("groups").document("Gusties")

        println("Initializing Gusties group...") // Debugging

        groupsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                println("Gusties group already exists")
                Gusties = document.toObject(Group::class.java)
            } else {
                println("Creating Gusties group...") // Debugging
                val defaultGroup = Group(name = "Gusties", creatorId = null, members = mutableListOf())
                groupsRef.set(defaultGroup)
                    .addOnSuccessListener { println("Gusties group created successfully!") }
                    .addOnFailureListener { e -> println("Error creating Gusties group: ${e.message}") }
                Gusties = defaultGroup
            }
        }.addOnFailureListener { e ->
            println("Error checking Gusties group: ${e.message}")
        }
    }
}