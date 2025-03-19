package com.example.gustiehub

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GroupsActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        val createGroupButton = findViewById<ImageButton>(R.id.create_groups_button)

        createGroupButton.setOnClickListener {
            NewGroupDialog()

        }

    }
    private fun NewGroupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_group_dialog,null)
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
}}
