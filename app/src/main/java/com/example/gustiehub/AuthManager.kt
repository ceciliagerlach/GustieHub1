package com.example.gustiehub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun signUpUser(email: String, password: String, firstName: String, lastName: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // create the user profile
                        val curStudent = Student(userId, email, firstName, lastName)
                        curStudent.createUserProfile(userId, email, firstName, lastName) { success, message ->
                            if (success) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, message)
                            }
                        }
                    } else {
                        onComplete(false, "User ID not found.")
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }
}