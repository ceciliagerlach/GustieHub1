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
import android.widget.SearchView
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
    private val allPosts = mutableListOf<Post>()

    val db = FirebaseFirestore.getInstance()

    // searchbar variables
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_discussion_fragment, container, false)
    }

    private fun listenForPostUpdates() {
        db.collection("posts")
            .whereEqualTo("group", groupName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error fetching posts", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                allPosts.clear()
                postList.clear()
                for (document in snapshot!!.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        allPosts.add(post)
                        postList.add(post)
                    }
                }
                postsAdapter.notifyDataSetChanged()
            }
    }

    private fun filterPosts(query: String): List<Post> {
        return allPosts.filter { post ->
            post.creatorName.contains(query, ignoreCase = true) ||
                    post.text.contains(query, ignoreCase = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = LinearLayoutManager(activity)
        searchView = view.findViewById(R.id.searchView)

        postsAdapter = PostAdapter(postList,
            onUsernameClick = { userId ->
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        )
        postsRecyclerView.adapter = postsAdapter

        listenForPostUpdates()

        FirebaseFirestore.getInstance()
            .collection("posts")
            .whereEqualTo("group", groupName)

        val searchHelper = SearchHelper(
            context = requireContext(),
            searchView = searchView,
            recyclerView = postsRecyclerView,
            adapter = postsAdapter,
            dataList = postList,
            filterFunction = ::filterPosts,
            updateFunction = { filtered -> postsAdapter.updatePosts(filtered) }
        )

        val createPostButton = view?.findViewById<ImageButton>(R.id.create_posts_button)

        fun newPostDialog(){
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.new_post_dialog, null)
            val editTextPost = dialogView.findViewById<EditText>(R.id.newPostContent)
            val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
            val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
            val enableCommentsSwitch = dialogView.findViewById<Switch>(R.id.commentSwitch)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create()
            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }
            buttonConfirm.setOnClickListener {
                //switch to enable comments should be moved to the dialog box.
                val postContent = editTextPost.text.toString()
                val commentsEnabled = enableCommentsSwitch.isChecked
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
                            val postRef = db.collection("posts")
                                .whereEqualTo("creatorName", username)
                                .whereEqualTo("group", groupName)
                                .whereEqualTo("text", postContent)
                                .limit(1)
                            postRef.get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        val postDocument = querySnapshot.documents[0]
                                        val postID = postDocument.id
                                        if (commentsEnabled) {
                                            userObject.enableComments(postID) { success, error ->
                                                if (success) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Post Created with comments enabled",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Failed to enable comments: $error",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                dialog.dismiss()
                                            }
                                        } else {
                                            userObject.disableComments(postID) { success, error ->
                                                if (success) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Post Created with comments disabled",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Failed to disable comments: $error",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                dialog.dismiss()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Post not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Error finding post: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
        if (createPostButton != null) {
            createPostButton.setOnClickListener {
                newPostDialog()
            }
        }
    }
}
