package com.example.gustiehub

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Year

class User(private val _userId: String,
           private val _email: String,
           private val _firstName: String,
           private val _lastName: String,
           private val _gradYear: Int,
           private val _homeState: String,
           private val _areasOfStudy: String) {

    // initialize values for FireBase
    private val userId = _userId
    private val email = _email
    private var firstName = _firstName
    private var lastName = _lastName
    private  var gradYear = _gradYear
    private var homeState = _homeState
    private var areasOfStudy = _areasOfStudy

    private lateinit var profilePicture: String // store as URL?
    private var joinedGroups: MutableList<String> = mutableListOf("Gusties") // store group names

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

//    init {
//        // automatically save student data
//        this.joinGroup("Gusties")
//    }

    // save new student info to Firestore
    fun createUserProfile(userId: String, email: String, firstName: String,
                          lastName: String, gradYear: Int, homeState: String,
                          areasOfStudy: String, onComplete: (Boolean, String?) -> Unit) {
        val userData = hashMapOf(
            "userID" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "gradYear" to gradYear,
            "homeState" to homeState,
            "areasOfStudy" to areasOfStudy,
            "joinedGroups" to mutableListOf<String>() // You can add default group here if needed
        )

        // add user to FireBase
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                println("User profile created for $userId")
                this.joinGroup("Gusties") // add new user to Gusties group
//                addUserToGustiesGroup(userId)

                //add user to class year group
                gradYear?.let { year ->
                    val classGroupName = "Class of $year"
                    //check to see if class group exists
                    val classGroupRef = db.collection("groups").document(classGroupName).get()
                    classGroupRef.addOnSuccessListener { document ->
                        if (document.exists()) {
                            // class group exists, add user to it
                            this.joinGroup(classGroupName)
                        }
                        else {
                            // class group doesn't exist, create it
                            val group = Group(classGroupName, userId)
                            group.createGroup()
                            this.joinGroup(classGroupName)

                        }
                    }
                }
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                println("Error creating user profile: ${e.message}")
                onComplete(false, e.message)
            }

        // add user to local dictionary
        GlobalData.userDict[userId] = this
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

    fun setGradYear(_gradYear: Int) {
        this.gradYear = _gradYear
        updateField("gradYear", _gradYear)
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
    fun getGradYear(): Int = gradYear
    fun getJoinedGroups(): List<String> = joinedGroups


    fun createPost(group: String, text: String) {
        val postData = hashMapOf(
            "creatorId" to userId,
            "group" to group,
            "text" to text,
            "timestamp" to System.currentTimeMillis() // add timestamp for ordering
        )

        db.collection("posts")
            .add(postData) // firestore generates a unique ID
            .addOnSuccessListener { documentReference ->
                println("Post created with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error creating post: ${e.message}")
            }
    }

    fun editPost(postID: String, newText: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user?.let {
            val userID = it.uid
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val postCreatorId = document.getString("creatorId")

                    // only the original creator can edit the post
                    if (postCreatorId == userID) {
                        postRef.update("text", newText)
                            .addOnSuccessListener {
                                println("Post $postID updated successfully.")
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                println("Error updating post: ${e.message}")
                                onComplete(false, e.message)
                            }
                    } else {
                        println("User $userID is not the creator of post $postID.")
                        onComplete(false, "You do not have permission to edit this post.")
                    }
                } else {
                    println("Post $postID not found.")
                    onComplete(false, "Post not found.")
                }
            }.addOnFailureListener { e ->
                println("Error retrieving post: ${e.message}")
                onComplete(false, e.message)
            }
        } ?: run {
            println("No authenticated user found.")
            onComplete(false, "No authenticated user found.")
        }
    }

}


// TODO: Create function leaveGroup
