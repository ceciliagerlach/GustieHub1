package com.example.gustiehub

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
    private var userList: List<User>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profilePictureImageView: ImageView = itemView.findViewById(R.id.profile_picture)
        var nameTextView: TextView = itemView.findViewById(R.id.user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        FirebaseFirestore.getInstance().collection("users").document().get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstname = document.getString("firstName")
                    val lastname = document.getString("lastName")
                    holder.nameTextView.text = firstname + " " + lastname
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

    fun updateUsers(newUsers: List<User>) {
        userList = newUsers
        notifyDataSetChanged()
    }

    override fun getItemCount() = userList.size

}