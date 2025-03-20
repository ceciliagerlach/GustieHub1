package com.example.gustiehub

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupsActivity : AppCompatActivity(){
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter
    private val groupsList = mutableListOf<String>()
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        val createGroupButton = findViewById<ImageButton>(R.id.create_groups_button)

        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        groupsAdapter = GroupsAdapter(groupsList)
        groupsRecyclerView.adapter = groupsAdapter

        //firebase fucntion for listening from firebase
        listenForGroupsUpdates()

        createGroupButton.setOnClickListener {
            NewGroupDialog()

        }

    }
    private fun listenForGroupsUpdates() {
        db.collection("groups")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error fetching groups", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                groupsList.clear()
                for (document in snapshot!!.documents) {
                    val groupName = document.getString("name")
                    if (groupName != null) {
                        groupsList.add(groupName)
                    }
                }
                groupsAdapter.notifyDataSetChanged()
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
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid
                    val group = Group(groupName, userId)
                    group.createGroup()
                    val currentUser = User(userId, user.email ?: "", "", "")
                    currentUser.joinGroup(groupName)
                    Toast.makeText(this, "Group Created: $groupName", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
                }
            } else {
                editTextGroupName.error = "Group name cannot be empty"
            }
        }
            dialog.show()
        }
    }
