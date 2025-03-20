package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupsAdapter(
    private var groupList: List<Group>,
    private val onItemClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.group_name_text)
        val descriptionTextView: TextView = itemView.findViewById(R.id.group_description_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_item, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.nameTextView.text = group.name
        holder.descriptionTextView.text = group.description
        holder.itemView.setOnClickListener {
            onItemClick(group)
        }
    }

    fun updateGroups(newGroups: List<Group>) {
        groupList = newGroups
        notifyDataSetChanged()
    }

    override fun getItemCount() = groupList.size
}
