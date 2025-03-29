package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.gustiehub.GroupPageActivity.Companion.GROUP_NAME
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupPageActivity : AppCompatActivity() {
    // variables for recycler view, displaying list of groups and posts
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostAdapter
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val postList = mutableListOf<Post>()
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private val db = FirebaseFirestore.getInstance()
    lateinit var groupName: String

    companion object {
        const val GROUP_NAME = "com.example.gustiehub.name"

        fun newIntent(packageContext: Context, group_name: String): Intent? {
            return Intent(packageContext, GroupPageActivity::class.java).apply {
                putExtra(GROUP_NAME, group_name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_page)
        groupName = intent.getStringExtra("groupName").toString()

        // get and set group name
        val groupNameTextView = findViewById<TextView>(R.id.group_name_text)
        groupNameTextView.text = groupName

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
        GlobalData.getFilteredGroupList(userID) { updatedGroups ->
            runOnUiThread {
                groupList.clear()
                groupList.addAll(updatedGroups)
                menuAdapter.updateGroups(updatedGroups)
            }
        }

        // initialize and handle clicks for message and profile buttons
        val messageButton: ImageView = findViewById(R.id.messaging)
        val profileButton: ImageView = findViewById(R.id.profile)
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // opening menu
        val menuButton: ImageView = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
            drawerLayout.openDrawer(GravityCompat.START)
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

        // setting up view pager
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this, groupName)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Discussion"
                1 -> "Information"
                else -> null
            }
        }.attach()

    }
}