package com.example.gustiehub

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gustiehub.EventsAdapter.EventsViewHolder
import com.google.firebase.firestore.FirebaseFirestore

class UserAdapter(
    private var userList: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    fun setSelectedUser(userId: String?) {
        selectedUserId = userId
        notifyDataSetChanged()
    }

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

//    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profile_picture)
//        val nameTextView: TextView = itemView.findViewById(R.id.user_name)
//    }

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
                        onUserClick(user)
                        notifyDataSetChanged()
                    }

//                    // visual changes to show user is selected
//                    if (user.userId == selectedUserId) {
//                        holder.itemView.setBackgroundColor(Color.GRAY)
//                    } else {
//                        holder.itemView.setBackgroundColor(Color.WHITE)
//                    }
                }
            }



        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

//    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
//        val user = userList[position]
//        holder.bind(user, user.userId == selectedUserId)
//        holder.itemView.setOnClickListener {
//            selectedUserId = user.userId
//            onUserClick(user)
//            setSelectedUser(selectedUserId)
//            notifyDataSetChanged()
//        }
//    }

    override fun getItemCount() = userList.size

    private var selectedUserId: String? = null

    fun updateUsers(newUsers: List<User>) {
        userList = newUsers
        notifyDataSetChanged()
    }


}