package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private var commentList: List<Post.Comment>,
//    private val onReportClick: (String) -> Unit // Callback for reporting a comment
    private val onCommentLongClick: (Post.Comment) -> Unit  // for editing a comment
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.user_name)
        val commentTextView: TextView = itemView.findViewById(R.id.comment_text)
        val reportButton: ImageButton = itemView.findViewById(R.id.report_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false) // Ensure the XML file is named correctly
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
//        holder.reportButton.setOnClickListener {
//            onReportClick(comment.commentId)
//        }
        holder.commentTextView.setOnLongClickListener {
            onCommentLongClick(comment)
            true
        }
    }


    override fun getItemCount() = commentList.size

    fun updateComments(newComments: List<Post.Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }
}
