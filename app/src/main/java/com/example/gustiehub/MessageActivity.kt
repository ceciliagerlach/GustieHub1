package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MessageActivity: AppCompatActivity() {
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        // list of groups in tab
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        menuRecyclerView = findViewById(R.id.recycler_menu)
        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuAdapter = MenuAdapter(filteredGroupList) { selectedGroup ->
            val intent = Intent(this, GroupPageActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        }
        menuRecyclerView.adapter = menuAdapter
        GlobalData.getFilteredGroupList(userID){ updatedGroups ->
            runOnUiThread {
                groupList.clear()
                groupList.addAll(updatedGroups)
                menuAdapter.updateGroups(updatedGroups)
            }
        }

        //set up drawer layout and handle clicks for menu items
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.tab_layout)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
                R.id.announcements -> {
                    val intent = Intent(this, AnnouncementsActivity::class.java)
                    startActivity(intent)
                }
                R.id.marketplace -> {
                    val intent = Intent(this, MarketplaceActivity::class.java)
                    startActivity(intent)
                }
                R.id.events -> {
                    val intent = Intent(this, EventsActivity::class.java)
                    startActivity(intent)
                }
                R.id.groups -> {
                    val intent = Intent(this, GroupsActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // opening menu
        val menuButton: ImageView = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // initialize and handle clicks for profile button
        val profileButton: ImageView = findViewById(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // ******* Functions ************************************
        fun getOrCreateConversation(userId1: String, userId2: String, onComplete: (String?) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            // find conversation
            db.collection("conversations")
                // get conversation IDs containing the first user's ID
                .whereArrayContains("userIds", userId1)
                .get()
                .addOnSuccessListener { snapshot ->
                    // search for the convo containing the second user's ID too
                    val existing = snapshot.documents.firstOrNull { it.get("userIds") is List<*> && (it["userIds"] as List<*>).contains(userId2) }
                    if (existing != null) {
                        onComplete(existing.id)
                        // create a new convo isntance if none exist
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

        fun sendMessage(conversationId: String, senderId: String, text: String) {
            val db = FirebaseFirestore.getInstance()
            // create message instance
            val message = hashMapOf(
                "senderId" to senderId,
                "text" to text,
                "timestamp" to Timestamp.now()
            )

            // find convo with both participants and add message
            val conversationRef = db.collection("conversations").document(conversationId)
            conversationRef.collection("messages")
                .add(message)
                .addOnSuccessListener {
                    conversationRef.update(
                        mapOf(
                            "lastMessage" to text,
                            "lastUpdated" to Timestamp.now()
                        )
                    )
                }
        }

        fun fetchMessages(conversationId: String, onComplete: (List<Message>) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            // get conversation
            db.collection("conversations").document(conversationId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { snapshot ->
                    val messages = snapshot.documents.mapNotNull { doc ->
                        val senderId = doc.getString("senderId")
                        val text = doc.getString("text")
                        val timestamp = doc.getTimestamp("timestamp")
                        if (senderId != null && text != null && timestamp != null) {
                            Message(senderId, text, timestamp)
                        } else null
                    }
                    onComplete(messages)
                }
        }

    }
}

