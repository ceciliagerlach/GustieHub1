package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupsAdapter(
    private var groupList: MutableList<Group>,
    private val onItemClick: (Group) -> Unit,
    //parameter for join group listener function
    private val onJoinGroupClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.group_name_text)
        val descriptionTextView: TextView = itemView.findViewById(R.id.group_description_text)
        val joinGroupButton: Button = itemView.findViewById(R.id.join_group_button)
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
        holder.joinGroupButton.setOnClickListener {
            onJoinGroupClick(group)
        }
    }

    // refresh group list
    fun updateGroups(newGroups: List<Group>) {
        groupList.clear()
        groupList.addAll(newGroups)
        notifyDataSetChanged()
    }


    override fun getItemCount() = groupList.size
}
