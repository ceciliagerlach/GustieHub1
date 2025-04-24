package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

// Marketplace adapter to bind listing data to views
class MarketplaceAdapter(
        private var itemList: List<Marketplace>,
        private val onUsernameClick: (String) -> Unit
    ): RecyclerView.Adapter<MarketplaceAdapter.MarketViewHolder>() {

    class MarketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.user_name)
        val itemNameTextView: TextView = itemView.findViewById(R.id.item)
        val priceTextView: TextView = itemView.findViewById(R.id.item_price)
        val descriptionTextView: TextView = itemView.findViewById(R.id.listing_description)
        val itemPhotoImageView: ImageView = itemView.findViewById(R.id.item_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listing_item, parent, false)
        return MarketViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        val item = itemList[position]
        holder.userNameTextView.text = item.userName
        holder.userNameTextView.setOnClickListener {
            onUsernameClick(item.userID)
        }
        holder.itemNameTextView.text = item.itemName
        holder.priceTextView.text = item.price
        holder.descriptionTextView.text = item.description

        Glide.with(holder.itemView.context)
            .load(item.itemPhotoURL)
            .placeholder(R.drawable.red_sofa) // optional placeholder
            .into(holder.itemPhotoImageView)
    }

    fun updateItems(newItems: List<Marketplace>) {
        itemList = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = itemList.size
}

