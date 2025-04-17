package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnnouncementsActivity : AppCompatActivity(){
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var announcementRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private val announcementList = mutableListOf<Announcement>()
    private val db = FirebaseFirestore.getInstance()

    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    // variables for searchbar
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var marketplaceAdapter: AnnouncementAdapter
    private lateinit var marketplaceList: MutableList<Announcement>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement)

        // list of announcements
        announcementRecyclerView = findViewById<RecyclerView>(R.id.announcementsRecyclerView)
        announcementRecyclerView.layoutManager = LinearLayoutManager(this)
        announcementAdapter = AnnouncementAdapter(announcementList)
        announcementRecyclerView.adapter = announcementAdapter
        GlobalData.getAnnouncements { updatedAnnouncements ->
            runOnUiThread {
                announcementList.clear()
                announcementList.addAll(updatedAnnouncements)
                announcementAdapter.updateAnnouncements(updatedAnnouncements)
            }
        }

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
                    drawerLayout.closeDrawer(GravityCompat.START)
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

        // handling clicks for toolbar
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

        val recyclerView: RecyclerView = findViewById(R.id.announcementsRecyclerView)
        val searchView: SearchView = findViewById(R.id.searchView)

        announcementAdapter = AnnouncementAdapter(announcementList)
        recyclerView.adapter = announcementAdapter

        listenForAnnouncementUpdates()

        val searchHelper = SearchHelper(
            context = this,
            searchView = searchView,
            recyclerView = recyclerView,
            adapter = announcementAdapter,
            dataList = announcementList,
            filterFunction = ::filterAnnouncements,
            updateFunction = { filtered -> announcementAdapter.updateAnnouncements(filtered) }
        )

    }

    private fun filterAnnouncements(query: String): List<Announcement> {
        return announcementList.filter { item ->
            item.header.contains(query, ignoreCase = true) ||
                    item.text.contains(query, ignoreCase = true)
        }
    }

    private fun listenForAnnouncementUpdates() {
        db.collection("announcements")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error fetching announcements", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                announcementList.clear()
                for (document in snapshot!!.documents) {
                    val announcement = document.toObject(Announcement::class.java)
                    if (announcement != null) {
                        announcementList.add(announcement)
                    }
                }
                announcementAdapter.notifyDataSetChanged()
            }
    }
}
