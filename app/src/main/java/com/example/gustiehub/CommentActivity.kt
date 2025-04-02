package com.example.gustiehub

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentInput: EditText
    private lateinit var commentButton: ImageButton
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        // Get postId from intent
        postId = intent.getStringExtra("postId") ?: return

        // Set up RecyclerView
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(emptyList()) { commentId ->
            // Handle comment reporting if needed
        }
        commentsRecyclerView.adapter = commentAdapter
        commentInput = findViewById(R.id.write_comment) // Make sure this ID matches your XML
        commentButton = findViewById(R.id.comment_button)

        // Fetch comments from Firestore
        fetchComments()

        // Set onClickListener for comment submission
        commentButton.setOnClickListener {
            submitComment()
        }
    }

    private fun fetchComments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for comments", error)
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val commentsList = document.get("comments") as? List<Map<String, Any>> ?: emptyList()

                    Log.d("Firestore", "Live updated comments: $commentsList")

                    val comments = commentsList.mapNotNull { commentMap ->
                        val commentId = commentMap["commentId"] as? String ?: return@mapNotNull null
                        val userId = commentMap["userId"] as? String ?: return@mapNotNull null
                        val text = commentMap["text"] as? String ?: return@mapNotNull null
                        val timestamp = commentMap["timestamp"] as? Timestamp
                        Post.Comment(commentId, userId, text, timestamp)
                    }

                    commentAdapter.updateComments(comments)
                    commentAdapter.notifyDataSetChanged()
                }
            }
    }


    private fun submitComment() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return
        val commentText = commentInput.text.toString().trim()
        val db = FirebaseFirestore.getInstance()

        if (commentText.isEmpty()) {
            return
        }

        // fetch user info to get their name
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            val firstName = document.getString("firstName") ?: ""
            val lastName = document.getString("lastName") ?: ""
            val fullName = "$firstName $lastName"

            val userObject = User(userId, "", firstName, lastName, 0, "", "")
            userObject.commentOnPost(postId, commentText) { success, errorMessage ->
                if (success) {
                    // clear input field and refresh comments
                    commentInput.text.clear()
                    fetchComments()
                } else {
                    Log.e("Firestore", "Failed to add comment: $errorMessage")
                }
            }
        }
    }

}
