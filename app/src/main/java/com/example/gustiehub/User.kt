package com.example.gustiehub

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Year
import java.util.UUID

// User class with attributes
class User(private val _userId: String,
           private val _email: String,
           private val _firstName: String,
           private val _lastName: String,
           private val _gradYear: Int,
           private val _homeState: String,
           private val _areasOfStudy: String) {

    // Initialize values for FireBase
    val userId = _userId
    private val email = _email
    var firstName = _firstName
    var lastName = _lastName
    private var gradYear = _gradYear
    private var homeState = _homeState
    private var areasOfStudy = _areasOfStudy

    var profilePicture = "https://firebasestorage.googleapis.com/v0/b/gustiehub.firebasestorage.app/o/profile_images%2Fdefault-profile-pic.png?alt=media&token=bec09d7b-a74d-484c-93b4-f0b1716d60bc"
    private var joinedGroups: MutableList<String> = mutableListOf("Gusties") // store group names

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Function to create user profile on new user login
    fun createUserProfile(
        userId: String, email: String, firstName: String,
        lastName: String, gradYear: Int, homeState: String,
        areasOfStudy: String, profilePictureURL: String, onComplete: (Boolean, String?) -> Unit
    ) {
        """ Saves new student info to Firestore""".trimMargin()
        val userData = hashMapOf(
            "userID" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "gradYear" to gradYear,
            "homeState" to homeState,
            "areasOfStudy" to areasOfStudy,
            "joinedGroups" to mutableListOf<String>(),
            "profilePicture" to profilePictureURL
        )

        // Add user to FireBase
        db.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                println("User profile created for $userId")
                this.joinGroup("Gusties") // Add new user to Gusties group

                // Add user to class year group
                gradYear.let { year ->
                    val classGroupName = "Class of $year"
                    // Check to see if class group exists
                    val classGroupRef = db.collection("groups").document(classGroupName).get()
                    classGroupRef.addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Class group exists, add user to it
                            this.joinGroup(classGroupName)
                        } else {
                            // Class group doesn't exist, create it
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

        // Add user to local dictionary
        GlobalData.userDict[userId] = this
    }

    // Function to add user to a group
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

    // Function to create a post
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

    // Function to edit a post
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

    // Function to delete a post
    fun deletePost(postID: String, onComplete: (Boolean, String?) -> Unit){
        val user = auth.currentUser
        user?.let {
            val userID = it.uid
            val postRef = db.collection("posts").document(postID)
            // only the original creator can delete the post
            postRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val postCreatorId = document.getString("creatorId")
                        if (postCreatorId != userID) {
                            println("User $userID is not the creator of post $postID.")
                            onComplete(false, "You do not have permission to delete this post.")
                            return@addOnSuccessListener
                        }

                        postRef.delete()
                            .addOnSuccessListener {
                                println("Post $postID deleted successfully.")
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                println("Error deleting post: ${e.message}")
                                onComplete(false, e.message)
                            }
                    } else {
                        println("Post $postID not found.")
                        onComplete(false, "Post not found.")
                    }
                }
                .addOnFailureListener { e ->
                    println("Error retrieving post: ${e.message}")
                    onComplete(false, e.message)
                }

        } ?: run {
            println("No authenticated user found.")
            onComplete(false, "No authenticated user found.")
        }
    }

    // Function to comment on a post
    fun commentOnPost(postID: String, comment: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user?.let {
            val userID = it.uid
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val newComment = mapOf(
                        "commentId" to UUID.randomUUID()
                            .toString(), // automatically generates a unique Id
                        "userId" to userID,
                        "text" to comment,
                        "timestamp" to Timestamp.now()
                    )

                    postRef.update("comments", FieldValue.arrayUnion(newComment))
                        .addOnSuccessListener {
                            println("Comment added successfully.")
                            onComplete(true, null)
                        }
                        .addOnFailureListener { e ->
                            println("Error updating post: ${e.message}")
                            onComplete(false, e.message)
                        }
                } else {
                    println("Post does not exist.")
                    onComplete(false, "Post does not exist.")
                }
            }.addOnFailureListener { e ->
                println("Error fetching post: ${e.message}")
                onComplete(false, e.message)
            }
        } ?: onComplete(false, "User not authenticated.")
    }

    // Function to disable comments on a post
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

    // Function to enable/disable comments on a post
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

    // Function to edit a comment
    fun editComment(
        postID: String,
        commentID: String,
        newComment: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // ensure its the commenter that wants to edit the comment
                    val comments = document.get("comments") as? MutableList<Map<String, Any>>
                        ?: mutableListOf()
                    val commentToEdit = comments.find { it["commentId"] == commentID }

                    val commentUserID = commentToEdit?.get("userId") as? String
                    if (commentUserID == user.uid) {
                        val updatedComments = comments.map { comment ->
                            if (comment["commentId"] == commentID) {
                                comment.toMutableMap().apply {
                                    this["text"] = newComment
                                    this["timestamp"] = Timestamp.now()
                                }
                                } else {
                                comment
                            }
                        }
                        postRef.update("comments", updatedComments)
                            .addOnSuccessListener {
                                println("Comment updated successfully.")
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                println("Error updating comment: ${e.message}")
                                onComplete(false, e.message)
                            }
                    } else {
                        onComplete(false, "You do not have permission to edit this comment.")
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

    // Function to delete a comment
    fun deleteComment(postID: String, commentID: String, onComplete: (Boolean, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            val postRef = db.collection("posts").document(postID)

            postRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val comments = document.get("comments") as? MutableList<Map<String, Any>>
                        ?: mutableListOf()
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
}
