package com.example.gustiehub

import com.example.gustiehub.AuthManager.addUserToGustiesGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Year

class Student(private val _userId: String,
              private val _email: String,
              private val _firstName: String,
              private val _lastName: String) {
    private val userId = _userId
    private val email = _email
    private var firstName = _firstName
    private var lastName = _lastName
    private lateinit var profilePictureUrl: String // store as URL
    private lateinit var gradYear: Year
    private var joinedGroups: MutableList<String> = mutableListOf("Gusties") // store group names

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        // Automatically save student data
    }

    // save new student info to Firestore
    fun createUserProfile(userId: String, email: String, firstName: String, lastName: String, onComplete: (Boolean, String?) -> Unit) {
        val userData = hashMapOf(
            "userID" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "joinedGroups" to mutableListOf<String>() // You can add default group here if needed
        )

        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                println("User profile created for $userId")
                addUserToGustiesGroup(userId) // add new user to Gusties group
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                println("Error creating user profile: ${e.message}")
                onComplete(false, e.message)
            }
    }

    // Setters
    fun setFirstName(_firstName: String) {
        this.firstName = _firstName
        updateField("firstName", _firstName)
    }

    fun setLastName(_lastName: String) {
        this.lastName = _lastName
        updateField("lastName", _lastName)
    }

    fun setProfilePictureUrl(_profilePictureUrl: String) {
        this.profilePictureUrl = _profilePictureUrl
        updateField("profilePictureUrl", _profilePictureUrl)
    }

    fun setGradYear(_gradYear: Year) {
        this.gradYear = _gradYear
        updateField("gradYear", _gradYear.toString())
    }

    fun joinGroup(groupID: String) {
        // grab user
        val user = auth.currentUser
        user?.let {
            val userID = it.uid

            // Reference to user and group in Firestore
            val userRef = db.collection("users").document(userID)
            val groupRef = db.collection("groups").document(groupID)

            // Update user's joinedGroups
            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val joinedGroups = document.get("joinedGroups") as? MutableList<String> ?: mutableListOf()
                    if (!joinedGroups.contains(groupID)) {
                        joinedGroups.add(groupID)
                        userRef.update("joinedGroups", joinedGroups)
                            .addOnSuccessListener { println("$userID joined $groupID") }
                            .addOnFailureListener { e -> println("Error updating user: ${e.message}") }
                    }
                }
            }

            // Update group's members
            groupRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val members = document.get("members") as? MutableList<String> ?: mutableListOf()
                    if (!members.contains(userID)) {
                        members.add(userID)
                        groupRef.update("members", members)
                            .addOnSuccessListener { println("$userID added to $groupID") }
                            .addOnFailureListener { e -> println("Error updating group: ${e.message}") }
                    }
                } else {
                    println("Group $groupID does not exist!")
                }
            }
        }
    }

    private fun updateField(field: String, value: Any) {
        db.collection("students").document(email)
            .update(field, value)
            .addOnSuccessListener { println("Updated $field for $email") }
            .addOnFailureListener { e -> println("Error updating $field: ${e.message}") }
    }

    // Getters
    fun getFirstName(): String =  firstName
    fun getLastName(): String = lastName
    fun getProfilePictureUrl(): String = if (::profilePictureUrl.isInitialized) profilePictureUrl else ""
    fun getGradYear(): Year? = if (::gradYear.isInitialized) gradYear else null
}

// TODO: Create function leaveGroup