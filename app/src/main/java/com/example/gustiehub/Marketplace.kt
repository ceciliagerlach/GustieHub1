package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore

// Marketplace data class with userName, userID, itemName, price, description, itemPhotoURL, timestamp, and itemID attributes
data class Marketplace (
    val userName: String = "",
    val userID: String = "",
    val itemName: String = "" ,
    val price: String = "",
    val description: String = "",
    val itemPhotoURL: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val itemID: String = ""
    ) {
private val db = FirebaseFirestore.getInstance()

    // Function to create item
    fun createItemListing() {
        val itemData = hashMapOf(
            "userName" to userName,
            "userID" to userID,
            "itemName" to itemName,
            "price" to price,
            "description" to description,
            "itemPhotoURL" to itemPhotoURL,
            "timestamp" to timestamp,
            "itemID" to itemID

        )

        db.collection("items").document(itemID)
            .set(itemData)
            .addOnSuccessListener { println("Item Name: $itemName") }
            .addOnFailureListener { e -> println("Error creating event: ${e.message}") }
    }
}