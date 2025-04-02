package com.example.gustiehub

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object GlobalData {
    var groupList = mutableListOf<Group>()
    var recentPosts = mutableListOf<Post>()

    // will these be used? Not sure yet
    var userDict = mutableMapOf<String,User>()
    var groupDict = mutableMapOf<String, Group>()

    fun getGroupList(userId: String, onGroupsUpdated: (List<Group>) -> Unit) {
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
                        if (userId !in group.members) {
                            if (!group.name.matches(Regex("^Class of \\d{4}$"))) {
                                updatedGroups.add(group)
                            }
                        }
                    }
                }
                println("Fetched ${updatedGroups.size} groups from Firestore.")
                onGroupsUpdated(updatedGroups) // update views accordingly
            }
        }
    }

//    fun fetchRecentPosts(limit: Long = 20) {
//        """ Fetches most recent posts (defaults to 20) and updates the global variable
//            |recentPosts accordingly.
//            |@input limit: the # of most recent posts to fetch
//            |@return: None
//        """.trimMargin()
//
//        val db = FirebaseFirestore.getInstance()
//        val postsRef = db.collection("posts")
//            .orderBy("timestamp", Query.Direction.DESCENDING) // fetch most recent posts first
//            .limit(limit)   // only fetch limit many posts
//
//        postsRef.addSnapshotListener { snapshots, e ->
//            if (e != null) {
//                println("Error fetching recent posts: ${e.message}")
//            }
//        }
//    }

    fun getFilteredGroupList(userId: String, onGroupsUpdated: (List<Group>) -> Unit) {
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
                        if (userId in group.members) {
                            updatedGroups.add(group)
                        }
                    }
                }
                println("Fetched ${updatedGroups.size} groups from Firestore.")
                onGroupsUpdated(updatedGroups) // update views accordingly
            }
        }
    }

    fun getPosts(groupName: String, onPostsUpdated: (List<Post>) -> Unit) {
        println("FirestoreDebug getPosts() called")
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        postsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for post changes: ${e.message}")
                return@addSnapshotListener
            }

            snapshots?.let {
                val updatedPosts = mutableListOf<Post>()
                for (document in it.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        if (groupName == post.group) {
                            val postId = document.id
                            updatedPosts.add(post.copy(postId = postId))
                        }
                    }
                }
                println("Fetched ${updatedPosts.size} posts from Firestore.")
                onPostsUpdated(updatedPosts) // update views accordingly
            }
        }
    }

    fun getComments(postId: String, onCommentsUpdated: (List<Post.Comment>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    println("Error getting comments: ${e.message}")
                    return@addSnapshotListener
                }

                snapshots?.let {
                    val comments = it.documents.mapNotNull { doc ->
                        doc.toObject(Post.Comment::class.java)
                    }
                    onCommentsUpdated(comments)
                }
            }
    }



    fun getEvents(onEventsUpdated: (List<Event>) -> Unit) {
        println("FirestoreDebug getEvents() called")
        val db = FirebaseFirestore.getInstance()
        val eventsRef = db.collection("events")
            .orderBy("date", Query.Direction.ASCENDING)
        eventsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for event changes: ${e.message}")
                return@addSnapshotListener
            }

            snapshots?.let {
                val updatedPosts = mutableListOf<Event>()
                val currentDate = Calendar.getInstance().time

                for (document in it.documents) {
                    val event = document.toObject(Event::class.java)
                    if (event != null && isFutureOrToday(event.date)) {
                            updatedPosts.add(event)
                    }
                }
                println("Fetched ${updatedPosts.size} events from Firestore.")
                onEventsUpdated(updatedPosts) // update views accordingly
            }
        }
    }

    /**
     * Converts a date string (e.g., "April 12") to a Date object and checks if it's today or in the future.
     */
    fun isFutureOrToday(dateStr: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("MMMM d", Locale.ENGLISH)
            val eventDate = dateFormat.parse(dateStr)

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val eventCalendar = Calendar.getInstance().apply {
                time = eventDate!!
                set(Calendar.YEAR, currentYear) 
            }

            val today = Calendar.getInstance()
            !eventCalendar.before(today)
        } catch (e: ParseException) {
            println("Error parsing date: $dateStr")
            false
        }
    }

}