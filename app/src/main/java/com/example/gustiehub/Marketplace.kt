package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore

data class Marketplace (
    val userName: String = "",
    val userID: String = "",
    val itemName: String = "" ,
    val price: String = "",
    val description: String = "",
    val itemPhotoURL: String = "",
    val time: String = "",
    val itemID: String = ""
    ) {
private val db = FirebaseFirestore.getInstance()

    fun createItemListing() {
        val itemData = hashMapOf(
            "userName" to userName,
            "userID" to userID,
            "itemName" to itemName,
            "price" to price,
            "description" to description,
            "itemPhotoURL" to itemPhotoURL,
            "time" to time,
            "itemID" to itemID

        )

        db.collection("items").document(itemID)
            .set(itemData)
            .addOnSuccessListener { println("Item Name: $itemName") }
            .addOnFailureListener { e -> println("Error creating event: ${e.message}") }
    }
}