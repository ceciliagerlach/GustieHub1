package com.example.gustiehub

import android.app.AlertDialog
import android.content.Context
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
import java.text.SimpleDateFormat
import java.util.Locale

// Fragment for group discussion
class GroupDiscussionFragment(val groupName: String) : Fragment() {
    // Variables for post recycler view
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val allPosts = mutableListOf<Post>()

    // Firebase variables
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    private val userObject = User(userId.toString(), "", "", "", 0, "", "")

    // Searchbar variables
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_discussion_fragment, container, false)
    }

    // Function to listen for post updates
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

    // Function to filter posts based on search query
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
            },
            onEditClick = { post -> showEditDialog(post) },
            onDeleteClick = { post -> removePost(post) },
            onReportClick = { post -> reportPost(requireContext(), post) }
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

        val createPostButton = view.findViewById<ImageButton>(R.id.create_posts_button)

        // Fetch and display posts
        GlobalData.getPosts(groupName) { updatedPosts ->
            requireActivity().runOnUiThread {
                postsAdapter.updatePosts(updatedPosts)
            }
        }

        // Function to create a new post in dialog box
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

    // Function to show edit dialog for a post
    private fun showEditDialog(post: Post) {
        val editText = EditText(requireContext())
        editText.setText(post.text)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Post")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    userObject?.editPost(post.postId, newText) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(requireContext(), "Post updated", Toast.LENGTH_SHORT).show()
                            GlobalData.getPosts(groupName) { updatedPosts ->
                                requireActivity().runOnUiThread {
                                    postsAdapter.updatePosts(updatedPosts)
                                }
                            }
                            } else {
                                Log.e("Firestore", "Failed to add comment: $errorMessage")
                            }
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to remove a post
    private fun removePost(post: Post) {
        userObject?.deletePost(post.postId){
            success, errorMessage ->
            if (success) {
                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                GlobalData.getPosts(groupName) { updatedPosts ->
                    requireActivity().runOnUiThread {
                        postsAdapter.updatePosts(updatedPosts)
                    }
                }
                } else {
                Log.e("Firestore", "Failed to add comment: $errorMessage")
            }
        }
    }

    // Function to report a post
    private fun reportPost(context: Context, post: Post) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        val formattedTimestamp = post.timestamp?.toDate()?.let { sdf.format(it) } ?: "Unknown time"
        // create an Intent to send an email with the report
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@gustiehub.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Report: Post")
            putExtra(
                Intent.EXTRA_TEXT, """
                A post has been reported.

                Post ID: ${post.postId}
                Creator ID: ${post.creatorId}
                Creator Name: ${post.creatorName}
                Text: ${post.text}
                Timestamp: $formattedTimestamp
            """.trimIndent()
            )
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Log.e("Report", "No email client found", e)
        }
    }

}
