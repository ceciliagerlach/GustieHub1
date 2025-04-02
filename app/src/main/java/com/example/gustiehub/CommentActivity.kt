package com.example.gustiehub

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
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

        // Fetch comments from Firestore
        fetchComments()
    }

    private fun fetchComments() {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                val commentsList = document.get("comments") as? List<Map<String, Any>> ?: emptyList()
                val comments = commentsList.mapNotNull { commentMap ->
                    val commentId = commentMap["commentId"] as? String ?: return@mapNotNull null
                    val userId = commentMap["userId"] as? String ?: return@mapNotNull null
                    val text = commentMap["text"] as? String ?: return@mapNotNull null
                    val timestamp = commentMap["timestamp"] as? Timestamp ?: Timestamp.now() // Default if missing

                    Post.Comment(commentId, userId, text, timestamp)
                }
                runOnUiThread {
                    commentAdapter.updateComments(comments)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching comments", e)
            }
    }
}
