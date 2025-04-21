package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import android.text.TextWatcher
import android.text.Editable
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.gustiehub.UserAdapter

class MessageActivity: AppCompatActivity() {
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var messageAdapter: MessageAdapter
    private val groupList = mutableListOf<Group>()
    private val chatList = mutableListOf<Conversation>()
    private val filteredGroupList = mutableListOf<Group>()
    private val TAG = "Message Activity"
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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

        // list of conversations with other users
        messageRecyclerView = findViewById(R.id.chatRecyclerView)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(chatList) { selectedChat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putStringArrayListExtra("userIds", ArrayList(selectedChat.userIds))
            startActivity(intent)
        }
        messageRecyclerView.adapter = messageAdapter
        GlobalData.getConversations(userID){ updatedChats ->
            runOnUiThread {
                chatList.clear()
                chatList.addAll(updatedChats)
                messageAdapter.updateChats(updatedChats)
            }
        }

        // set up create new conversation button
        val createChatButton = findViewById<ImageButton>(R.id.create_chat_button)
        createChatButton.setOnClickListener {
            NewChatDialog()
        }
    }

    // ******* Functions ************************************
    fun getOrCreateConversation(userId1: String, userId2: String, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val sortedUserIds = listOf(userId1, userId2).sorted()
        val conversationId = "${sortedUserIds[0]}_${sortedUserIds[1]}"

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
                        "userIds" to sortedUserIds,
                        "lastMessage" to "",
                        "lastUpdated" to Timestamp.now()
                    )
                    db.collection("conversations").document(conversationId)
                        .set(newConversation)
                        .addOnSuccessListener { onComplete(conversationId) }
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

    // dialog box to start chat with new user
    private fun NewChatDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_chat_dialog, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchUser)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.usersRecyclerView)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val allUsers = mutableListOf<User>()
        val filteredUsers = mutableListOf<User>()
        var selectedUser: User? = null
        val userAdapter = UserAdapter(userList = emptyList()) { user ->
            selectedUser = user
        }

        recyclerView.adapter = userAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        GlobalData.getUsers { users ->
            GlobalData.getUserConversations(currentUserId) { conversationUserIds ->
                val filtered = users.filter { user ->
                    user.userId != currentUserId && !conversationUserIds.contains(user.userId)
                }

                allUsers.clear()
                allUsers.addAll(filtered)
                filteredUsers.clear()
                filteredUsers.addAll(filtered)
                userAdapter.updateUsers(filtered)
            }
        }

        // Real-time search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase() ?: ""
                val results = allUsers.filter {
                    "${it.firstName} ${it.lastName}".lowercase().contains(query)
                }
                filteredUsers.clear()
                filteredUsers.addAll(results)
                userAdapter.updateUsers(filteredUsers)
                return true
            }
        })

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonConfirm.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val selectedUserId = selectedUser?.userId
            if (selectedUserId != null) {
                getOrCreateConversation(currentUserId, selectedUserId) { conversationId ->
                    conversationId?.let {
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putStringArrayListExtra("userIds", ArrayList(listOf(currentUserId, selectedUserId)))
                        startActivity(intent)
                    } ?: run {
                        Log.w(TAG, "Failed to create conversation")
                    }
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}

