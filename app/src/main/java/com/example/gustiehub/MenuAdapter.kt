package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Menu adapter to bind tab/menu data to views
class MenuAdapter (
    private var menuItems: List<Group>,
    private val onItemClick: (Group) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.group_name_text)
        fun bind(item: String) {
            nameTextView.text = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tab_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuItems[position].toString())
        val group = menuItems[position]
        holder.nameTextView.text = group.name
        holder.itemView.setOnClickListener {
            onItemClick(group)
        }
    }

    fun updateGroups(newGroups: List<Group>) {
        menuItems = newGroups
        notifyDataSetChanged()
    }
    override fun getItemCount() = menuItems.size
}