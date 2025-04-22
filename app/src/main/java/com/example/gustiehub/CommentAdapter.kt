package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Comment adapter to bind comment data to views
class CommentAdapter(
    private var commentList: List<Comment>,
    private val onEditClick: (Comment) -> Unit,
    private val onDeleteClick: (Comment) -> Unit,
    private val onReportClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.user_name)
        val commentTextView: TextView = itemView.findViewById(R.id.comment_text)
        val moreButton: ImageButton = itemView.findViewById(R.id.menu_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        // fetch username from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(comment.userId).get()
            .addOnSuccessListener { document ->
                holder.usernameTextView.text = document.getString("firstName") + " " +
                        document.getString("lastName")
            }
            .addOnFailureListener {
                holder.usernameTextView.text = "Unknown User"
            }

        holder.commentTextView.text = comment.text
        holder.moreButton.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val popupMenu = PopupMenu(holder.itemView.context, holder.moreButton)
            popupMenu.inflate(R.menu.edit_delete_options_menu)
            if (comment.userId != currentUserId) {
                // remove edit/delete options if not user's post
                popupMenu.menu.removeItem(R.id.menu_edit)
                popupMenu.menu.removeItem(R.id.menu_delete)
            }
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        onEditClick(comment)
                        true
                    }
                    R.id.menu_delete -> {
                        onDeleteClick(comment)
                        true
                    }
                    R.id.menu_report -> {
                        onReportClick(comment)
                        true
                    }
                    else -> false
                    }
                }
            popupMenu.show()
        }
    }

    override fun getItemCount() = commentList.size

    fun updateComments(newComments: List<Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }
}
