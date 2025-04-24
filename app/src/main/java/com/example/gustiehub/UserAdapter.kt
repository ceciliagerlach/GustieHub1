package com.example.gustiehub

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gustiehub.EventsAdapter.EventsViewHolder
import com.google.firebase.firestore.FirebaseFirestore

// User adapter to bind user data to views
class UserAdapter(
    private var userList: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.user_name)

        fun bind(user: User, isSelected: Boolean) {
            nameTextView.text = "${user.firstName} ${user.lastName}"
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.selected_user_background
                else R.drawable.default_user_background
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val userId = user.userId  // Make sure User class exposes this

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstname = document.getString("firstName")
                    val lastname = document.getString("lastName")
                    holder.nameTextView.text = "$firstname $lastname"

                    val user = userList[position]
                    holder.bind(user, user.userId == selectedUserId)
                    holder.itemView.setOnClickListener {
                        selectedUserId = user.userId
                        val previousSelected = selectedPosition
                        selectedPosition = holder.adapterPosition
                        notifyItemChanged(previousSelected)
                        notifyItemChanged(selectedPosition)
                        onUserClick(user)
                        notifyDataSetChanged()
                    }

                    // Visual changes to show user is selected
                    if (position == selectedPosition) {
                        holder.nameTextView.paintFlags = holder.nameTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gold))
                    } else {
                        holder.nameTextView.paintFlags = holder.nameTextView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                        holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                    }
                }
            }

        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount() = userList.size

    private var selectedUserId: String? = null

    fun updateUsers(newUsers: List<User>) {
        userList = newUsers
        notifyDataSetChanged()
    }


}