package com.example.gustiehub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GroupInformationFragment(val groupName: String) : Fragment() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_information_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // get and set group information
        val groupDescriptionTextView = view.findViewById<TextView>(R.id.groupDescription)
        val groupMembers = view.findViewById<TextView>(R.id.groupMembers)
        db.collection("groups").document(groupName).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val description = document.getString("description")
                    groupDescriptionTextView.text = description
                    val members = document.get("members") as List<String>
                    lifecycleScope.launch {
                        val memberNames = getMemberNames(members)
                        println("Member Names: $memberNames")
                        groupMembers.text = memberNames.joinToString("\n")
                    }
                }
            }
    }
    suspend fun getMemberNames(members: List<String>): List<String> {
        val memberNames = mutableListOf<String>()

        for (member in members) {
            println("Member ID: $member")
            val userDocument = db.collection("users").document(member).get().await()
            if (userDocument.exists()) {
                val firstName = userDocument.getString("firstName") ?: ""
                val lastName = userDocument.getString("lastName") ?: ""
                val fullName = "$firstName $lastName"
                memberNames.add(fullName)
                println("Updated List: $memberNames")
            }
        }
        println("Member Names: $memberNames")
        return memberNames
    }

}