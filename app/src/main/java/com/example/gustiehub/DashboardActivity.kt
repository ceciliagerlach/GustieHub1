package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale


class DashboardActivity : AppCompatActivity() {
    // Variables for recycler views
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private val db = FirebaseFirestore.getInstance()

    // Variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    // Variables for displaying 3 most recent announcements + events + posts
    private lateinit var announcementPreview1: TextView
    private lateinit var announcementPreview2: TextView
    private lateinit var announcementPreview3: TextView
    private lateinit var eventPreview1: TextView
    private lateinit var eventPreview2: TextView
    private lateinit var eventPreview3: TextView

    // Passing email to new intent for login
    companion object {
        private const val EXTRA_EMAIL = "com.example.gustiehub.email"

        fun newIntent(packageContext: Context, email: String): Intent? {
            return Intent(packageContext, DashboardActivity::class.java).apply {
                putExtra("USER_EMAIL", email)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize text views
        announcementPreview1 = findViewById(R.id.announcement_preview1)
        announcementPreview2 = findViewById(R.id.announcement_preview2)
        eventPreview1 = findViewById(R.id.event_preview1)
        eventPreview2 = findViewById(R.id.event_preview2)

        // Fetch two most recent of each
        fetchRecentAnnouncements(2)
        fetchRecentEvents(2)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        fetchRecentGroupPosts(userId,3)

        // List of groups in tab
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

        // Set up drawer layout and handle clicks for menu items
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.tab_layout)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dashboard -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
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

        // Initialize buttons reference
        val messageButton: ImageView = findViewById(R.id.messaging)
        val profileButton: ImageView = findViewById(R.id.profile)
        val menuButton: ImageView = findViewById(R.id.menu)
        val announcementsButton: Button = findViewById(R.id.see_all_announcements_button)
        val eventsButton: Button = findViewById(R.id.see_all_events_button)

        // Handling clicks for buttons
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        menuButton.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
            drawerLayout.openDrawer(GravityCompat.START)
        }
        announcementsButton.setOnClickListener {
            val intent = Intent(this, AnnouncementsActivity::class.java)
            startActivity(intent)
        }
        eventsButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to fetch recent announcements
    private fun fetchRecentAnnouncements(limit: Long) {
        db.collection("announcements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)   // only display the limit most recent
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Dashboard", "No announcements found")
                } else {
                    Log.d("Dashboard", "Announcements retrieved: ${documents.documents}")
                }

                val announcements = documents.map {
                    val header = it.getString("header") ?: "No Content"
                    val text = it.getString("text") ?: "No Content"
                    "$header -- $text"
                }
                runOnUiThread {
                    announcementPreview1.text = announcements.getOrNull(0) ?: "No announcements"
                    announcementPreview2.text = announcements.getOrNull(1) ?: "No announcements"
                }
            }
            .addOnFailureListener { e ->
                Log.e("Dashboard", "Error fetching announcements", e)
            }
    }

    // Function to fetch recent events
    private fun fetchRecentEvents(limit: Long) {
        db.collection("events")
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Dashboard", "No posts found")
                } else {
                    Log.d("Dashboard", "Posts retrieved: ${documents.documents}")
                }

                // Filter only future or today's events
                val futureEvents = documents
                    .filter {
                        val date = it.getString("date") ?: return@filter false
                        GlobalData.isFuture(date)
                    }
                    .take(limit.toInt()) // only take up to `limit` events

                // Map to display format
                val posts = futureEvents.map {
                    val eventName = it.getString("eventName") ?: "No Name"
                    val date = it.getString("date") ?: "No Date"
                    "$date -- $eventName"
                }

                runOnUiThread {
                    eventPreview1.text = posts.getOrNull(0) ?: "No events"
                    eventPreview2.text = posts.getOrNull(1) ?: "No events"
                }
            }
            .addOnFailureListener { e ->
                Log.e("Dashboard", "Error fetching posts", e)
            }
    }

    // Function to fetch recent group posts
    private fun fetchRecentGroupPosts(userId: String, limit: Long) {

        // get groups the user has joined
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val joinedGroups = document.get("joinedGroups") as? List<String> ?: emptyList()
                Log.d("Firestore", "User's joined groups: $joinedGroups")

                if (joinedGroups.isEmpty()) {
                    Log.d("Firestore", "User has not joined any groups.")
                    updateActivityPreviews(emptyList())
                    return@addOnSuccessListener
                }

                // if user is in more than 10 groups, handle queries in batches
                val groupsBatch = if (joinedGroups.size > 10) joinedGroups.take(10) else joinedGroups

                Log.d("Firestore", "Fetching posts from groups: $groupsBatch")

                // fetch posts from these groups
                db.collection("posts")
                    .whereIn("group", joinedGroups) // filter by joined groups
                    .orderBy("timestamp", Query.Direction.DESCENDING) // get most recent posts
                    .limit(limit)
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val recentPosts = postsSnapshot.documents.mapNotNull { doc ->
                            val group = doc.getString("group")
                            val postText = doc.getString("text")
                            "$group -- $postText"
                        }

                        Log.d("Firestore", "Fetched recent posts: $recentPosts")
                        updateActivityPreviews(recentPosts)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error fetching posts", e)
                        updateActivityPreviews(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user joined groups", e)
                updateActivityPreviews(emptyList())
            }
    }

    // Function to update the UI with posts or "No recent activity" if none found
    private fun updateActivityPreviews(posts: List<String>) {
        val activityPreview1 = findViewById<TextView>(R.id.activity_preview1)
        val activityPreview2 = findViewById<TextView>(R.id.activity_preview2)
        val activityPreview3 = findViewById<TextView>(R.id.activity_preview3)

        activityPreview1.text = posts.getOrNull(0) ?: "No recent activity"
        activityPreview2.text = posts.getOrNull(1) ?: "No recent activity"
        activityPreview3.text = posts.getOrNull(2) ?: "No recent activity"
        Log.d("UI", "Updated activity previews: ${activityPreview1.text}, ${activityPreview2.text}, ${activityPreview3.text}")
    }

}