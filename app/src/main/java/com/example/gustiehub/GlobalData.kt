package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object GlobalData {
    var groupList = mutableListOf<Group>()
    var recentPosts = mutableListOf<Post>()

    // will these be used? Not sure yet
    var userDict = mutableMapOf<String,User>()
    var groupDict = mutableMapOf<String, Group>()

    fun getGroupList(onGroupsUpdated: (List<Group>) -> Unit) {
        """ Fetches created groups and updates the global variable groupList accordingly.
            |@return: None
        """.trimMargin()

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

    fun fetchRecentPosts(limit: Long = 20) {
        """ Fetches most recent posts (defaults to 20) and updates the global variable
            |recentPosts accordingly.
            |@input limit: the # of most recent posts to fetch
            |@return: None
        """.trimMargin()

        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING) // fetch most recent posts first
            .limit(limit)   // only fetch limit many posts

        postsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error fetching recent posts: ${e.message}")
            }
        }
    }
    fun getFilteredGroupList(userId: String, onGroupsUpdated: (List<Group>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val groupsRef = db.collection("groups")

        groupsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for group changes: ${e.message}")
                return@addSnapshotListener
            }

            snapshots?.let {
                val updatedPosts = mutableListOf<Post>()
                for (document in it.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        updatedPosts.add(post)
                    }
                }

                synchronized(recentPosts) {
                    recentPosts.clear()
                    recentPosts.addAll(updatedPosts)
                }
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
//                val updatedGroups = mutableListOf<Group>()
//                for (document in it.documents) {
//                    val group = document.toObject(Group::class.java)
//                    if (group != null) {
//                        if (userId in group.members) {
//                            updatedGroups.add(group)
//                        }
//                    }
//                }
//                println("Fetched ${updatedGroups.size} groups from Firestore.")
//                onGroupsUpdated(updatedGroups) // update views accordingly
//            }
//        }


}