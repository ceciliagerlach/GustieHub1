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

class ChatAdapter(
    private var messageList: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.chat_message)
        val messageDateTextView: TextView = itemView.findViewById(R.id.message_date)
        val messageTimeTextView: TextView = itemView.findViewById(R.id.message_time)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profile_picture)
        val messageTextView: TextView = itemView.findViewById(R.id.chat_message)
        val messageDateTextView: TextView = itemView.findViewById(R.id.message_date)
        val messageTimeTextView: TextView = itemView.findViewById(R.id.message_time)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            SentMessageViewHolder(inflater.inflate(R.layout.user_message_item, parent, false))
        } else {
            ReceivedMessageViewHolder(inflater.inflate(R.layout.other_message_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        val formattedDate = SimpleDateFormat("MMMM d", Locale.getDefault()).format(message.timestamp.toDate())
        val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.timestamp.toDate())

        when (holder) {
            is SentMessageViewHolder -> {
                holder.messageTextView.text = message.text
                holder.messageDateTextView.text = formattedDate
                holder.messageTimeTextView.text = formattedTime
            }

            is ReceivedMessageViewHolder -> {
                holder.messageTextView.text = message.text
                holder.messageDateTextView.text = formattedDate
                holder.messageTimeTextView.text = formattedTime

                // load profile picture
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
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
        }

    }

    override fun getItemCount() = messageList.size

    fun updateMessages(newMessages: List<Message>) {
        messageList = newMessages
        notifyDataSetChanged()
    }
}
