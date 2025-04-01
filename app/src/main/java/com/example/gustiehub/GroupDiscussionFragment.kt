package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
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
                showComment(postId) // Call function to show comment input
            }
        )
        postsRecyclerView.adapter = postsAdapter
        GlobalData.getPosts(groupName) { updatedPosts ->
            requireActivity().runOnUiThread {
                postsAdapter.updatePosts(updatedPosts)
            }
        }
    }
         fun showComment(postId: String) {
//            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_comment, null)
//            val editText = dialogView.findViewById<EditText>(R.id.commentEditText)
//
//            AlertDialog.Builder(requireContext())
//                .setTitle("Add a Comment")
//                .setView(dialogView)
//                .setPositiveButton("Post") { _, _ ->
//                    val commentText = editText.text.toString().trim()
//                    if (commentText.isNotEmpty()) {
//                        postComment(postId, commentText)
//                    }
//                }
//                .setNegativeButton("Cancel", null)
//                .show()

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
}