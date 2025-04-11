package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupDiscussionFragment(val groupName: String) : Fragment() {
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

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = LinearLayoutManager(activity)

        postsAdapter = PostAdapter(postList,
            onUsernameClick = { userId ->
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        )
        postsRecyclerView.adapter = postsAdapter

        // fetch and display posts
        GlobalData.getPosts(groupName) { updatedPosts ->
            requireActivity().runOnUiThread {
                postsAdapter.updatePosts(updatedPosts)
            }
        }
        // listen for new posts
        val createPostButton = view.findViewById<ImageButton>(R.id.create_posts_button)
        fun newPostDialog(){
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.new_post_dialog, null)
            val editTextPost = dialogView.findViewById<EditText>(R.id.newPostContent)
            val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
            val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()
            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }
            buttonConfirm.setOnClickListener {
                val postContent = editTextPost.text.toString()
                if (postContent.isNotEmpty()) {
                    val user = FirebaseAuth.getInstance().currentUser
                    //get user information from firebase instance
                    val username = user?.displayName.toString()
                    if (user != null) {
                        val userId = user.uid
                        val userObject = User(userId, "", "", "", 0, "", "")
                        //val userObject = GlobalData.userDict[userId]
                        if (userObject != null) {
                            userObject.createPost(username, groupName, postContent)
                        }
                        Toast.makeText(requireContext(), "Post Created", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "User not authenticated",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                else {
                    editTextPost.error = "Post cannot be empty"
                }
            }
            dialog.show()
        }
        createPostButton.setOnClickListener {
            newPostDialog()
        }

    }
}
