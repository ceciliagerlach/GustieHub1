package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity:  AppCompatActivity() {
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var chatAdapter: ChatAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private val messageList = mutableListOf<Message>()
    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private val TAG = "ChatActivity"
    private lateinit var conversationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging2)
        var userIds = intent.getStringArrayListExtra("userIds")

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

        // list of messages with user
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val otherUserId = userIds?.firstOrNull { it != userId } ?: return

        GlobalData.getOrCreateConversation(userId, otherUserId) { conversationId ->
            if (conversationId != null) {
                chatRecyclerView = findViewById(R.id.recycler_chat)
                chatRecyclerView.layoutManager = LinearLayoutManager(this)
                chatAdapter = ChatAdapter(messageList)
                chatRecyclerView.adapter = chatAdapter

                GlobalData.getMessages(conversationId) { updatedMessages ->
                    runOnUiThread {
                        messageList.clear()
                        messageList.addAll(updatedMessages)
                        chatAdapter.updateMessages(updatedMessages)
                    }
                }
            } else {
                Log.w(TAG, "Could not find or create conversation")
            }
        }

        // listener for sending a message
        val sendButton = findViewById<Button>(R.id.send_button)
        val messageInput = findViewById<EditText>(R.id.message_input)

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val receiverId = getReceiverId(userIds) // Fetch the receiver's ID
                sendMessage(conversationId, senderId, receiverId, messageText)
                messageInput.setText("") // Clear input after sending
            }
        }



    }

    private fun sendMessage(conversationId: String, senderId: String, receiverId: String, text: String) {
        val db = FirebaseFirestore.getInstance()

        // Create message instance
        val message = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to Timestamp.now(),
            "read" to false // Initially, the message is unread
        )

        // Add message to Firestore under the conversation
        db.collection("conversations").document(conversationId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // Update the last message in the conversation
                db.collection("conversations").document(conversationId)
                    .update(
                        mapOf(
                            "lastMessage" to text,
                            "lastUpdated" to Timestamp.now()
                        )
                    )
            }
    }

    private fun getReceiverId(userIds: ArrayList<String>): String {
        return userIds.first { it != FirebaseAuth.getInstance().currentUser?.uid }
    }


}