package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter (
    private var chatList: List<Conversation>,
    private val onItemClick: (Conversation) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profile_picture)
        val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.last_message)
        val lastMessageDateTextView: TextView = itemView.findViewById(R.id.last_message_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_chat_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chat = chatList[position]
        holder.lastMessageTextView.text = chat.lastMessage
        // format date correctly
        val dateFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(chat.lastUpdated.toDate())
        holder.lastMessageDateTextView.text = formattedDate
        holder.itemView.setOnClickListener {
            onItemClick(chat)
        }
        // get name of OTHER user in the conversation
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val otherUserId = chat.userIds.firstOrNull { it != userId }
        if (otherUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(otherUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstname = document.getString("firstName")
                        val lastname = document.getString("lastName")
                        holder.nameTextView.text = firstname + " " + lastname
                    }
                }
        }
        // load profile picture of OTHER user in the conversation
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
    }

    fun updateChats(newChats: List<Conversation>) {
        chatList = newChats
        notifyDataSetChanged()
    }

    override fun getItemCount() = chatList.size

}