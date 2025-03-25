package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gustiehub.GroupPageActivity.Companion.GROUP_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupPageActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val GROUP_NAME = "com.example.gustiehub.name"

        fun newIntent(packageContext: Context, group_name: String): Intent? {
            return Intent(packageContext, GroupPageActivity::class.java).apply {
                putExtra(GROUP_NAME, group_name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_page)
        val groupName = intent.getStringExtra("groupName").toString()

        // get and set group information
        val groupNameTextView = findViewById<TextView>(R.id.group_name_text)
        val groupDescriptionTextView = findViewById<TextView>(R.id.group_description_text)
        groupNameTextView.text = groupName
        db.collection("groups").document(groupName).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val description = document.getString("description")
                    groupDescriptionTextView.text = description
                }
            }
    }

}