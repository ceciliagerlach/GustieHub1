package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore

object GlobalData {
    var groupList = mutableListOf<Group>()

    fun getGroupList(onGroupsUpdated: (List<Group>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val groupsRef = db.collection("groups")

        groupsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for group changes: ${e.message}")
                return@addSnapshotListener
            }

            snapshots?.let {
                val updatedGroups = mutableListOf<Group>()
                for (document in it.documents) {
                    val group = document.toObject(Group::class.java)
                    if (group != null) {
                        updatedGroups.add(group)
                    }
                }
                println("Fetched ${updatedGroups.size} groups from Firestore.")
                onGroupsUpdated(updatedGroups) // update views accordingly
            }
        }
    }

//    fun onUpdate(updatedGroups: List<Group>) {
//        synchronized(groupList) { // prevents race conditions
//            groupList.clear()
//            groupList.addAll(updatedGroups)
//        }
//        recyclerViewAdapter.notifyDataSetChanged() // update recyclerView
//    }

}