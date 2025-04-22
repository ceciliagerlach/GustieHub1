package com.example.gustiehub

import android.widget.Toast
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

    fun getUsers(onUserUpdated: (List<User>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")
            .orderBy("firstName", Query.Direction.ASCENDING)

        usersRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for user changes: ${e.message}")
                return@addSnapshotListener
            }

            val updatedUsers = mutableListOf<User>()
            snapshots?.documents?.forEach { document ->
                val userId = document.id
                val email = document.getString("email") ?: ""
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val gradYear = document.getLong("gradYear")?.toInt() ?: 0
                val homeState = document.getString("homeState") ?: ""
                val areasOfStudy = document.getString("areasOfStudy") ?: ""

                val user = User(
                    _userId = userId,
                    _email = email,
                    _firstName = firstName,
                    _lastName = lastName,
                    _gradYear = gradYear,
                    _homeState = homeState,
                    _areasOfStudy = areasOfStudy
                )
                updatedUsers.add(user)
            }

            println("Fetched ${updatedUsers.size} users from Firestore.")
            onUserUpdated(updatedUsers)
        }
    }


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
                                println("${userId}: ${group.name}\nUpdated Groups Size: ${updatedGroups.size}")
                            }
                        }
                    }
                }
                println("Fetched ${updatedGroups.size} groups from Firestore.")
                onGroupsUpdated(updatedGroups) // update views accordingly
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

    fun getMarketplaceItems(onItemsUpdated: (List<Marketplace>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val itemsRef = db.collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        itemsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for marketplace changes: ${e.message}")
                return@addSnapshotListener
            }

            snapshots?.let {
                val updatedItems = mutableListOf<Marketplace>()
                for (document in it.documents) {
                    val item = document.toObject(Marketplace::class.java)
                    if (item!= null) {
                        updatedItems.add(item)
                    }
                }
                println("Fetched ${updatedItems.size} events from Firestore.")
                onItemsUpdated(updatedItems) // update views accordingly
            }
        }
    }

    fun getComments(postId: String, onCommentsUpdated: (List<Comment>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val commentsRef = db.collection("posts").document(postId)
        commentsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                println("Error getting comments: ${e.message}")
                return@addSnapshotListener
            }
            val commentsList = snapshot?.get("comments") as? List<Map<String, Any>> ?: emptyList()
            val comments = commentsList.mapNotNull { commentMap ->
                val commentId = commentMap["commentId"] as? String ?: return@mapNotNull null
                val userId = commentMap["userId"] as? String ?: return@mapNotNull null
                val text = commentMap["text"] as? String ?: return@mapNotNull null
                val timestamp = commentMap["timestamp"] as? Timestamp
                Comment(commentId, userId, text, timestamp)
            }
            onCommentsUpdated(comments)
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
                val updatedEvents = mutableListOf<Event>()
                val currentDate = Calendar.getInstance().time

                for (document in it.documents) {
                    val event = document.toObject(Event::class.java)
                    if (event != null && isFuture(event.date)) {
                            updatedEvents.add(event)
                    }
                }
                println("Fetched ${updatedEvents.size} events from Firestore.")
                onEventsUpdated(updatedEvents) // update views accordingly
            }
        }
    }

    fun getAnnouncements(onAnnouncementsUpdated: (List<Announcement>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val announcementsRef = db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        announcementsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                println("Error listening for announcement changes: ${e.message}")
                return@addSnapshotListener
            }
            snapshots?.let {
                val updatedAnnouncements = mutableListOf<Announcement>()
                for (document in it.documents) {
                    val announcement = document.toObject(Announcement::class.java)
                    if (announcement != null) {
                        updatedAnnouncements.add(announcement)
                    }
                }
                println("Fetched ${updatedAnnouncements.size} announcements from Firestore.")
                onAnnouncementsUpdated(updatedAnnouncements) // update views accordingly
            }
        }
    }
    fun getConversations(
        userId: String,
        onConversationsUpdated: (List<Conversation>) -> Unit
    ) {
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

    /**
     * Converts a date string (e.g., "April 12") to a Date object and checks if it's today or in the future.
     */
    fun isFuture(dateStr: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("MMMM d", Locale.ENGLISH)
            val eventDate = dateFormat.parse(dateStr)

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val eventCalendar = Calendar.getInstance().apply {
                time = eventDate!!
                set(Calendar.YEAR, currentYear)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // normalizes both today and eventCalendar to midnight, so no issues with comparing time
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            !eventCalendar.before(today) // return events today and in the future
        } catch (e: ParseException) {
            println("Error parsing date: $dateStr")
            false
        }
    }

    // might be redundant?
    fun getOrCreateConversation(
        userId1: String,
        userId2: String,
        onComplete: (String?) -> Unit
    ) {
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

    fun getUserConversations(currentUserId: String, onComplete: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")
            .whereArrayContains("userIds", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val otherUserIds = snapshot.documents.mapNotNull { doc ->
                    val userIds = doc.get("userIds") as? List<*>
                    userIds?.firstOrNull { it != currentUserId } as? String
                }
                onComplete(otherUserIds)
            }
            .addOnFailureListener { onComplete(emptyList()) }
    }

    fun getMessages(
        conversationId: String,
        onMessagesUpdated: (List<Message>) -> Unit
    ) {
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

}