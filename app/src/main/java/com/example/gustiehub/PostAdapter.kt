package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(
    private var postList: List<Post>,
    private val onUsernameClick: (String) -> Unit,
    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit,
    private val onReportClick: (Post, Context) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.user_name)
        val descriptionTextView: TextView = itemView.findViewById(R.id.post_text)
        val viewCommentsButton: TextView = itemView.findViewById(R.id.view_comments_button)
        val moreButton: ImageButton = itemView.findViewById(R.id.menu_button)
        val reportButton: ImageButton = itemView.findViewById(R.id.report_button)
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
        if (post.commentsEnabled) {
            holder.viewCommentsButton.visibility = View.VISIBLE
            holder.viewCommentsButton.setOnClickListener {
                Log.d("CommentButton", "Clicked, opening CommentActivity for postId: ${post.postId}")
                val intent = Intent(holder.itemView.context, CommentActivity::class.java).apply {
                    putExtra("postId", post.postId)
                    putExtra("groupName", post.group)
                }
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.viewCommentsButton.visibility = View.GONE
            holder.viewCommentsButton.setOnClickListener(null)
        }

        holder.reportButton.setOnClickListener {
            onReportClick(post, holder.itemView.context)
        }

        holder.moreButton.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.moreButton)
            popupMenu.inflate(R.menu.edit_delete_options_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        onEditClick(post)
                        true
                    }
                    R.id.menu_delete -> {
                        onDeleteClick(post)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    fun updatePosts(newPosts: List<Post>) {
        postList = newPosts
        notifyDataSetChanged()
    }

    override fun getItemCount() = postList.size
}
