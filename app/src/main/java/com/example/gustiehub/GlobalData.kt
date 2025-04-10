package com.example.gustiehub

import com.google.firebase.Timestamp
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

    fun getComments(postId: String, onCommentsUpdated: (List<Comment>) -> Unit) {
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
                        doc.toObject(Comment::class.java)
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

    fun getConversations(userId: String, onConversationsUpdated: (List<Conversation>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val conversationsRef = db.collection("conversations")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
         conversationsRef.addSnapshotListener { snapshots, e ->
             if (e != null) {
                 println("Error listening for chat changes: ${e.message}")
                 return@addSnapshotListener
             }

             snapshots?.let {
                 val updatedChats = mutableListOf<Conversation>()
                 for (document in it.documents) {
                     val chat = document.toObject(Conversation::class.java)
                     if (chat != null) {
                         if (userId in chat.userIds) {
                             updatedChats.add(chat)
                         }
                     }
                 }
                 println("Fetched ${updatedChats.size} events from Firestore.")
                 onConversationsUpdated(updatedChats) // update views accordingly
             }
         }
    }

    fun getOrCreateConversation(userId1: String, userId2: String, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")
            .whereArrayContains("userIds", userId1)
            .get()
            .addOnSuccessListener { snapshot ->
                val existing = snapshot.documents.firstOrNull { doc ->
                    val userIds = doc.get("userIds") as? List<*>
                    userIds?.contains(userId2) == true
                }
                if (existing != null) {
                    onComplete(existing.id)
                } else {
                    val newConversation = hashMapOf(
                        "userIds" to listOf(userId1, userId2),
                        "lastMessage" to "",
                        "lastUpdated" to Timestamp.now()
                    )
                    db.collection("conversations")
                        .add(newConversation)
                        .addOnSuccessListener { docRef -> onComplete(docRef.id) }
                        .addOnFailureListener { onComplete(null) }
                }
            }
            .addOnFailureListener { onComplete(null) }
    }

    fun getMessages(conversationId: String, onMessagesUpdated: (List<Message>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
        messagesRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for message changes: ${e.message}")
                return@addSnapshotListener
            }
            snapshots?.let {
                val updatedMessages = mutableListOf<Message>()
                for (document in it.documents) {
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        updatedMessages.add(message)
                    }
                }
                onMessagesUpdated(updatedMessages) // update views accordingly
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