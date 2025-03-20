package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gustiehub.GlobalData.groupList

class GroupsActivity : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter
    private val groupList = mutableListOf<Group>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        val createGroupButton = findViewById<ImageButton>(R.id.create_groups_button)

        createGroupButton.setOnClickListener {
            NewGroupDialog()
        }

        val recyclerView: RecyclerView = findViewById(R.id.groupsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GroupAdapter(groupList) { selectedGroup ->
            val intent = Intent(this, GroupsActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        GlobalData.getGroupList { updatedGroups ->
            runOnUiThread {
                if (updatedGroups.isEmpty()) {
                    println("No groups found!") // Debugging log
                } else {
                    println("Updating RecyclerView with ${updatedGroups.size} groups.") // Debugging log
                }
                groupList.clear()
                groupList.addAll(updatedGroups)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun NewGroupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_group_dialog, null)
        val editTextGroupName = dialogView.findViewById<EditText>(R.id.newGroupName)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonConfirm.setOnClickListener {
            val groupName = editTextGroupName.text.toString()
            if (groupName.isNotEmpty()) {
                Toast.makeText(this, "Group Created: $groupName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                editTextGroupName.error = "Group name cannot be empty"
            }

        }
        dialog.show()
    }

    class GroupAdapter(
        private val groupList: List<Group>,
        private val onItemClick: (Group) -> Unit
    ) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

        class GroupViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            val nameTextView: TextView = itemView.findViewById(R.id.group_name_text)
            val descriptionTextView: TextView = itemView.findViewById(R.id.group_description_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            // Create a new view, which defines the UI of the list item
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

        override fun getItemCount() = groupList.size
    }
}

