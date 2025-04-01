package com.example.gustiehub

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Year
import java.util.UUID

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

    fun createUserProfile(userId: String, email: String, firstName: String,
                          lastName: String, gradYear: Int, homeState: String,
                          areasOfStudy: String, profilePictureURL: String, onComplete: (Boolean, String?) -> Unit) {
        """ Saves new student info to Firestore""".trimMargin()
        val userData = hashMapOf(
            "userID" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "gradYear" to gradYear,
            "homeState" to homeState,
            "areasOfStudy" to areasOfStudy,
            "joinedGroups" to mutableListOf<String>(), // You can add default group here if needed
            "profilePicture" to profilePictureURL
        )

        // add user to FireBase
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                println("User profile created for $userId")
                this.joinGroup("Gusties") // add new user to Gusties group
//                addUserToGustiesGroup(userId)

                //add user to class year group
                gradYear.let { year ->
                    val classGroupName = "Class of $year"
                    //check to see if class group exists
                    val classGroupRef = db.collection("groups").document(classGroupName).get()
                    classGroupRef.addOnSuccessListener { document ->
                        if (document.exists()) {
                            // class group exists, add user to it
                            this.joinGroup(classGroupName)
                        } else {
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
        """ Updates the specified field to have the specified value of any
            | attribute of a student in Firestore
            | @param field: String
            | @param value: String
            | @return: None""".trimMargin()
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


    fun createPost(name: String, group: String, text: String) {
        val postData = hashMapOf(
            "creatorId" to userId,
            "creatorName" to name,
            "group" to group,
            "text" to text,
            "timestamp" to Timestamp.now(), // add timestamp for ordering
            "comments" to emptyList<Map<String, Any>>(),
            "commentsEnabled" to true
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

    fun commentOnPost(postID: String, comment: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user?.let {
            val userID = it.uid
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val newComment = mapOf(
                        "commentId" to UUID.randomUUID().toString(), // generate a unique comment ID
                        "userID" to userID,
                        "comment" to comment,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    postRef.update("comments", FieldValue.arrayUnion(newComment))
                        .addOnSuccessListener {
                            println("Comment added to post $postID successfully.")
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            println("Error updating post: ${e.message}")
                            onComplete(false, e.message)
                        }
                } else {
                    println("Post $postID does not exist.")
                    onComplete(false, "Post does not exist.")
                }
            }.addOnFailureListener { e ->
                println("Error fetching post: ${e.message}")
                onComplete(false, e.message)
            }
        } ?: onComplete(false, "User not authenticated.")
    } // end commentOnPost

    fun disableComments(postID: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user?.let {
            val userID = it.uid
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val postCreatorId = document.getString("creatorId")

                    // only the creator can disable comments
                    if (postCreatorId == userID) {
                        postRef.update("commentsEnabled", false)
                            .addOnSuccessListener {
                                println("Comments disabled on $postID.")
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                println("Error disabling comments: ${e.message}")
                                onComplete(false, e.message)
                            }
                    }
                } else {
                    println("Post $postID does not exist.")
                    onComplete(false, "Post does not exist.")
                }
            }.addOnFailureListener { e ->
                println("Error fetching post: ${e.message}")
                onComplete(false, e.message)
            }
        } ?: onComplete(false, "User not authenticated.")
    }

    fun enableComments(postID: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user?.let {
            val userID = it.uid
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val postCreatorId = document.getString("creatorId")

                    // only the creator can enable comments
                    if (postCreatorId == userID) {
                        postRef.update("commentsEnabled", true)
                            .addOnSuccessListener {
                                println("Comments enabled on $postID.")
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                println("Error enabling comments: ${e.message}")
                                onComplete(false, e.message)
                            }
                    }
                } else {
                    println("Post $postID does not exist.")
                    onComplete(false, "Post does not exist.")
                }
            }.addOnFailureListener { e ->
                println("Error fetching post: ${e.message}")
                onComplete(false, e.message)
            }
        } ?: onComplete(false, "User not authenticated.")
    }
}

fun editComment(postID: String, commentID: String, newComment: String, onComplete: (Boolean, String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    user?.let {
        val postRef = db.collection("posts").document(postID)

        postRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // ensure its the commenter that wants to edit the comment
                val comments = document.get("comments") as? MutableList<Map<String, Any>> ?: mutableListOf()
                val commentToEdit = comments.find { it["commentId"] == commentID }

                val commentUserID = commentToEdit?.get("userId") as? String
                if (commentUserID == user.uid) {
                    val updatedComments = comments.map { comment ->
                        if (comment["commentId"] == commentID) {
                            comment.toMutableMap().apply {
                                this["comment"] = newComment
                            }
                        }
                    }
                }

                postRef.update("comments", newComment)
                    .addOnSuccessListener {
                        println("Comment updated successfully.")
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        println("Error updating comment: ${e.message}")
                        onComplete(false, e.message)
                    }
            } else {
                println("Post $postID does not exist.")
                onComplete(false, "Post does not exist.")
            }
        }.addOnFailureListener { e ->
            println("Error fetching post: ${e.message}")
            onComplete(false, e.message)
        }
    } ?: onComplete(false, "User not authenticated.")
}



fun deleteComment(postID: String, commentID: String, onComplete: (Boolean, String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    user?.let {
        val postRef = db.collection("posts").document(postID)

        postRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val comments = document.get("comments") as? MutableList<Map<String, Any>> ?: mutableListOf()
                // ensure its the commenter that wants to edit the comment
                val commentToDelete = comments.find { it["commentId"] == commentID }
                val postCreatorID = document.getString("creatorId")

                val commentUserID = commentToDelete?.get("userId") as? String

                // only the post creator or commenter can delete their comment
                if (commentUserID == user.uid || postCreatorID == user.uid) {
                    val updatedComments = comments.filterNot { comment ->
                        comment["commentId"] == commentID
                    }

                    postRef.update("comments", updatedComments)
                        .addOnSuccessListener {
                            println("Comment deleted successfully.")
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            println("Error deleting comment: ${e.message}")
                            onComplete(false, e.message)
                        }
                }
            } else {
                println("Post $postID does not exist.")
                onComplete(false, "Post does not exist.")
            }
        }.addOnFailureListener { e ->
            println("Error fetching post: ${e.message}")
            onComplete(false, e.message)
        }
    } ?: onComplete(false, "User not authenticated.")
}



// TODO: Create function leaveGroup
// TODO: Create report functionality + button
