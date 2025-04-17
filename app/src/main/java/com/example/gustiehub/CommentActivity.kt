package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentInput: EditText
    private lateinit var commentButton: ImageButton
    private lateinit var postId: String
    private lateinit var groupName: String
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var postsAdapter: PostAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    private val userObject = User(auth.currentUser?.uid ?: "", "", "", "", 0, "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        // Get postId from intent
        postId = intent.getStringExtra("postId") ?: return
        groupName = intent.getStringExtra("groupName") ?: return
        commentInput = findViewById(R.id.write_comment)
        commentButton = findViewById(R.id.comment_button)

        // display post information
        val postUserName: TextView = findViewById(R.id.user_name)
        val postText: TextView = findViewById(R.id.post_text)
        val moreButton: ImageButton = findViewById(R.id.menu_button)
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("creatorName") ?: "Unknown User"
                    val text = document.getString("text") ?: "No Content"
                    postUserName.text = username
                    postText.text = text
                }
            }
        moreButton.setOnClickListener{
            val currentUserId = auth.currentUser?.uid ?: return@setOnClickListener
            val db = FirebaseFirestore.getInstance()
            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    val post = document.toObject(Post::class.java)
                    val popupMenu = PopupMenu(this, moreButton)
                    popupMenu.inflate(R.menu.edit_delete_options_menu)
                    if (post != null) {
                        if (post.creatorId != currentUserId) {
                            // remove edit/delete options if not user's post
                            popupMenu.menu.removeItem(R.id.menu_edit)
                            popupMenu.menu.removeItem(R.id.menu_delete)
                        }
                        popupMenu.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.menu_edit -> {
                                    showEditDialog(post)
                                    true
                                }
                                R.id.menu_delete -> {
                                    removePost(post)
                                    true
                                }
                                R.id.menu_report -> {
                                    reportPost(post)
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                    popupMenu.show()
                }
        }

        // Check if comments are enabled for the post
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val commentsEnabled = document.getBoolean("commentsEnabled") ?: true

                    if (!commentsEnabled) {
                        commentInput.isEnabled = false
                        commentButton.isEnabled = false
                        commentInput.hint = "Comments are disabled for this post"
                    }
                } else {
                    Log.d("CommentActivity", "No such post")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CommentActivity", "get failed with ", exception)
            }


        // Set up RecyclerView
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView)
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentAdapter = CommentAdapter(emptyList(),
            onEditClick = { comment -> showEditCommentDialog(comment) },
            onDeleteClick = { comment -> removeComment(comment) },
            onReportClick = { comment -> reportComment(comment) }
        )

        commentsRecyclerView.adapter = commentAdapter
        commentInput = findViewById(R.id.write_comment)
        commentButton = findViewById(R.id.comment_button)

        // Fetch comments from Firestore
        fetchComments()

        // Set onClickListener for comment submission
        commentButton.setOnClickListener {
            submitComment()
        }

        // set onClickListener for back button
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, GroupPageActivity::class.java)
            intent.putExtra("groupName", groupName)
            startActivity(intent)
        }

        // list of groups in tab
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        menuRecyclerView = findViewById(R.id.recycler_menu)
        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuAdapter = MenuAdapter(filteredGroupList) { selectedGroup ->
            val intent = Intent(this, GroupPageActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        }
        menuRecyclerView.adapter = menuAdapter
        GlobalData.getFilteredGroupList(userID) { updatedGroups ->
            runOnUiThread {
                groupList.clear()
                groupList.addAll(updatedGroups)
                menuAdapter.updateGroups(updatedGroups)
            }
        }

        //set up drawer layout and handle clicks for menu items
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.tab_layout)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }

                R.id.announcements -> {
                    val intent = Intent(this, AnnouncementsActivity::class.java)
                    startActivity(intent)
                }

                R.id.marketplace -> {
                    val intent = Intent(this, MarketplaceActivity::class.java)
                    startActivity(intent)
                }

                R.id.events -> {
                    val intent = Intent(this, EventsActivity::class.java)
                    startActivity(intent)
                }

                R.id.groups -> {
                    val intent = Intent(this, GroupsActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // opening menu
        val menuButton: ImageView = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // handling clicks for toolbar
        val messageButton: ImageView = findViewById(R.id.messaging)
        val profileButton: ImageView = findViewById(R.id.profile)
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    // show dialog to edit comment
    private fun showEditCommentDialog(comment: Post.Comment) {
        val editText = EditText(this)
        editText.setText(comment.text)

        AlertDialog.Builder(this)
            .setTitle("Edit Comment")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString()
                if (newText.isNotEmpty()) {
                    userObject.editComment(
                        postId,
                        comment.commentId,
                        newText
                    ) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(this, "Comment updated", Toast.LENGTH_SHORT).show()
                            fetchComments()
                        } else {
                            Log.e("Firestore", "Failed to add comment: $errorMessage")
                        }
                    }

                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


        private fun removeComment(comment: Post.Comment) {
            userObject.deleteComment(postId, comment.commentId) { success, errorMessage ->
                if (success) {
                    Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show()
                    fetchComments()
                } else {
                    Log.e("Firestore", "Failed to add comment: $errorMessage")
                }
            }
        }

        private fun reportComment(comment: Post.Comment) {
            //TODO: add report functionality
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
                        val commentsList =
                            document.get("comments") as? List<Map<String, Any>> ?: emptyList()

                        Log.d("Firestore", "Live updated comments: $commentsList")

                        val comments = commentsList.mapNotNull { commentMap ->
                            val commentId =
                                commentMap["commentId"] as? String ?: return@mapNotNull null
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

    // edit post
    private fun showEditDialog(post: Post) {
        val editText = EditText(this)
        editText.setText(post.text)

        AlertDialog.Builder(this)
            .setTitle("Edit Post")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    userObject?.editPost(post.postId, newText) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(this, "Post updated", Toast.LENGTH_SHORT).show()
                            GlobalData.getPosts(groupName) { updatedPosts ->
                                this.runOnUiThread {
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

    // remove post
    private fun removePost(post: Post) {
        userObject?.deletePost(post.postId){
                success, errorMessage ->
            if (success) {
                Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show()
                GlobalData.getPosts(groupName) { updatedPosts ->
                    this.runOnUiThread {
                        postsAdapter.updatePosts(updatedPosts)
                    }
                }
            } else {
                Log.e("Firestore", "Failed to add comment: $errorMessage")
            }
        }
    }

    // report post
    private fun reportPost(post: Post) {
        //TODO: add report functionality
    }
}