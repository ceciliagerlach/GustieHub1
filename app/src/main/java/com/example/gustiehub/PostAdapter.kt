package com.example.gustiehub

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private var postList: List<Post>,
    private val onUsernameClick: (String) -> Unit,
    private val onCommentClick: (String) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.user_name)
        val descriptionTextView: TextView = itemView.findViewById(R.id.post_text)
        val viewCommentsButton: TextView = itemView.findViewById(R.id.view_comments_button)
        val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        val commentAdapter = CommentAdapter(emptyList()) { commentId ->
            // handle comment report here if needed
        }

        init {
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            commentsRecyclerView.adapter = commentAdapter
            commentsRecyclerView.visibility = View.GONE // initially hide comments
            viewCommentsButton.isClickable = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.usernameTextView.text = post.creatorName
        holder.descriptionTextView.text = post.text
        holder.usernameTextView.setOnClickListener {
            onUsernameClick(post.creatorId)
        }
        holder.viewCommentsButton.setOnClickListener {
            Log.d("CommentButton", "Clicked, fetching comments for postId: ${post.postId}")
            if (holder.commentsRecyclerView.visibility == View.VISIBLE) {
                holder.commentsRecyclerView.visibility = View.GONE
            } else {
                GlobalData.getComments(post.postId) { comments ->
                    holder.commentsRecyclerView.visibility = View.VISIBLE
                    holder.commentAdapter.updateComments(comments)
                    Log.d("Comments", "Fetched comments: $comments")
                }
            }
        }

        // fetch comments for the current post
        GlobalData.getComments(post.postId) { comments ->
            holder.commentAdapter.updateComments(comments)
        }
    }

    fun updatePosts(newPosts: List<Post>) {
        postList = newPosts
        notifyDataSetChanged()
    }

    override fun getItemCount() = postList.size
}
