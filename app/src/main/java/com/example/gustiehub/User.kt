package com.example.gustiehub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Year

class User(private val _userId: String,
           private val _email: String,
           private val _firstName: String,
           private val _lastName: String) {
    private val userId = _userId
    private val email = _email
    private var firstName = _firstName
    private var lastName = _lastName
    private lateinit var profilePicture: String // store as URL?
    private lateinit var gradYear: Year
    private var joinedGroups: MutableList<String> = mutableListOf("Gusties") // store group names

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        // Automatically save student data
        this.joinGroup("Gusties")
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

        // add user to FireBase
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                println("User profile created for $userId")
                this.joinGroup("Gusties") // add new user to Gusties group
//                addUserToGustiesGroup(userId)
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                println("Error creating user profile: ${e.message}")
                onComplete(false, e.message)
            }

        // add user to local dictionary
        GlobalData.userDict.put(userId, this)
    }

//    fun addUserToGustiesGroup(userId: String) {
//        val groupsRef = db.collection("groups").document("Gusties")
//
//        groupsRef.update("members", FieldValue.arrayUnion(userId))
//            .addOnSuccessListener { println("User added to Gusties group.") }
//            .addOnFailureListener { it.printStackTrace() }
//    }

    // Setters
    fun setFirstName(_firstName: String) {
        this.firstName = _firstName
        updateField("firstName", _firstName)
    }

    fun setLastName(_lastName: String) {
        this.lastName = _lastName
        updateField("lastName", _lastName)
    }

    fun setProfilePicture(_profilePicture: String) {
        this.profilePicture = _profilePicture
        updateField("profilePicture", _profilePicture)
    }

    fun setGradYear(_gradYear: Year) {
        this.gradYear = _gradYear
        updateField("gradYear", _gradYear.toString())
    }

    fun joinGroup(groupID: String) {
        val user = auth.currentUser
        user?.let {
            val userID = it.uid
            val userRef = db.collection("users").document(userID)
            val groupRef = db.collection("groups").document(groupID)

            // Update user's joinedGroups
            userRef.update("joinedGroups", FieldValue.arrayUnion(groupID))
                .addOnSuccessListener { println("$userID successfully joined $groupID") }
                .addOnFailureListener { e -> println("Error updating user's joinedGroups: ${e.message}") }

            // Update group's members
            groupRef.update("members", FieldValue.arrayUnion(userID))
                .addOnSuccessListener { println("$userID successfully added to $groupID") }
                .addOnFailureListener { e -> println("Error updating group's members: ${e.message}") }
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
    fun getProfilePicture(): String = if (::profilePicture.isInitialized) profilePicture else ""
    fun getGradYear(): Year? = if (::gradYear.isInitialized) gradYear else null
}

// TODO: Create function leaveGroup
// TODO: Create function createGroup
// TODO: Create sign up screen requesting first + last name (prof pic can be done later)