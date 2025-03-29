package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class EventsActivity: AppCompatActivity() {
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var eventsAdapter: EventsAdapter
    private val groupList = mutableListOf<Group>()
    private val eventsList = mutableListOf<Event>()
    private val filteredGroupList = mutableListOf<Group>()
    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        // list of events
        eventsRecyclerView = findViewById<RecyclerView>(R.id.eventsRecyclerView)
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        eventsAdapter = EventsAdapter(eventsList)
        eventsRecyclerView.adapter = eventsAdapter
        GlobalData.getEvents { updatedEvents ->
            runOnUiThread {
                eventsList.clear()
                eventsList.addAll(updatedEvents)
                eventsAdapter.updateEvents(updatedEvents)
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
                    val intent = Intent(this, AnnouncementsActivity::class.java)
                    startActivity(intent)
                }
                R.id.marketplace -> {
                    val intent = Intent(this, MarketplaceActivity::class.java)
                    startActivity(intent)
                }
                R.id.events -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
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

    }
}
