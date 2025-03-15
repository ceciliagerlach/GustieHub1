package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore

object GlobalData {
    var Gusties: Group? = null

    fun initializeGlobalData() {
        val db = FirebaseFirestore.getInstance() // get Firestore instance inside the function
        val groupsRef = db.collection("groups").document("Gusties")

        groupsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Gusties = document.toObject(Group::class.java)
            } else {
                // create default group
                val defaultGroup = Group(name = "Gusties", creatorId = null, members = mutableListOf())
                groupsRef.set(defaultGroup)
                Gusties = defaultGroup
            }
        }.addOnFailureListener { it.printStackTrace() } // logging
    }
}