package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gustiehub.EventsAdapter.EventsViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter (
    private var chatList: List<Conversation>,
    private val onItemClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profile_picture)
        val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.last_message)
        val lastMessageDateTextView: TextView = itemView.findViewById(R.id.last_message_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.nameTextView.text = chat.nameOfUser
        holder.lastMessageTextView.text = chat.lastMessage
        // formate date correctly
        val dateFormat = SimpleDateFormat("MMMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(chat.lastUpdated.toDate())
        holder.lastMessageDateTextView.text = formattedDate
        holder.itemView.setOnClickListener {
            onItemClick(chat)
        }
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
    }

    fun updateChats(newChats: List<Conversation>) {
        chatList = newChats
        notifyDataSetChanged()
    }

    override fun getItemCount() = chatList.size

}