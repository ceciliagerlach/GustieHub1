package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupDiscussionFragment(val groupName: String) : Fragment() {
    // variables for recycler view, displaying list of groups and posts
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentsAdapter: CommentAdapter

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_discussion_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // list of posts on group page
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = LinearLayoutManager(activity)

        postsAdapter = PostAdapter(postList,
            onUsernameClick = { userId ->
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            },
            onCommentClick = { postId ->
                showComment(postId)
                fetchPostAndComments(postId)
            }
        )
        postsRecyclerView.adapter = postsAdapter
        GlobalData.getPosts(groupName) { updatedPosts ->
            requireActivity().runOnUiThread {
                postsAdapter.updatePosts(updatedPosts)
            }
        }

        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView)
        commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentsAdapter = CommentAdapter(emptyList()) { commentId ->
            // Handle reporting a comment
        }
        commentsRecyclerView.adapter = commentsAdapter

    }
    fun showComment(postId: String) {
        GlobalData.getComments(postId) { comments ->
            requireActivity().runOnUiThread {
                // Assuming you have a commentsRecyclerView in your fragment layout
                commentsRecyclerView.visibility = View.VISIBLE
                commentsAdapter.updateComments(comments)
            }
        }
    }

    fun fetchPostAndComments(postId: String) {
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(postId)

        postRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val post = document.toObject(Post::class.java)  // Your post object
                val commentsMap = document.get("comments") as? Map<String, Map<String, String>> // Fetch the comments map

                val comments = mutableListOf<Post.Comment>()

                // Deserialize the comments from the map
                commentsMap?.forEach { (commentId, commentData) ->
                    val comment = Post.Comment(
                        commentId = commentId,
                        userId = commentData["userId"] ?: "",
                        text = commentData["text"] ?: ""
                    )
                    comments.add(comment)
                }

                // Pass the comments to the comment adapter
                commentsAdapter.updateComments(comments)
            } else {
                // Handle case where post doesn't exist
            }
        }.addOnFailureListener { exception ->
            // Handle errors when fetching the post
            Log.e("Firestore", "Error getting post: ", exception)
        }
    }


//        // posting
//        val postText: EditText = requireView().findViewById(R.id.write_post)
//        val postButton: ImageButton = requireView().findViewById(R.id.post_button)
//        postButton.setOnClickListener {
//            if (postText.text.isNotEmpty()) {
//                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
//                db.collection("users").document(userId).get()
//                    .addOnSuccessListener { document ->
//                        val firstname = document.getString("firstName") ?: ""
//                        val lastname = document.getString("lastName") ?: ""
//                        val fullName = "$firstname $lastname"
//                        val user = User(userId, "", firstname, lastname, 0, "", "")
//                        user.createPost(fullName, groupName, postText.text.toString())
//                        Toast.makeText(requireContext(), "Post created", Toast.LENGTH_SHORT).show()
//                        postText.text.clear()
//                    }
//            }
//        }
}