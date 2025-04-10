package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gustiehub.MessageAdapter.MessageViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter (
    private var messageList: List<Message>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private val VIEW_TYPE_SENT = 1;
    private val VIEW_TYPE_RECEIVED = 2;

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profile_picture)
        val messageTextView: TextView = itemView.findViewById(R.id.chat_message)
        val messageDateTextView: TextView = itemView.findViewById(R.id.message_date)
        val messageTimeTextView: TextView = itemView.findViewById(R.id.message_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_message_item, parent, false)
            return ChatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.other_message_item, parent, false)
            return ChatViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messageList[position]
        holder.messageTextView.text = message.text
        // format date correctly
        val dateFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(message.timestamp.toDate())
        holder.messageDateTextView.text = formattedDate
        // format time correctly
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(message.timestamp.toDate())
        holder.messageTimeTextView.text = formattedTime
        if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            // load profile picture of OTHER user in the conversation
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profilePictureUrl = document.getString("profilePicture")
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(profilePictureUrl)
                                .into(holder.profilePictureImageView)
                        } else {
                            holder.profilePictureImageView.setImageResource(R.drawable.sample_profile_picture)
                        }
                    }
                }
        } else {
            holder.profilePictureImageView.visibility = View.GONE
        }

    }

    fun updateMessages(newMessages: List<Message>) {
        messageList = newMessages
        notifyDataSetChanged()
    }

    override fun getItemCount() = messageList.size

}