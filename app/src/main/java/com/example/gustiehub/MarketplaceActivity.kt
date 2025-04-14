package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
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

class MarketplaceActivity: AppCompatActivity(){
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private val db = FirebaseFirestore.getInstance()

    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    // variables for searchbar
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
//    private lateinit var marketplaceAdapter: MarketplaceAdapter
//    private lateinit var marketplaceList: MutableList<MarketplaceItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)

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
                    drawerLayout.closeDrawer(GravityCompat.START)
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

        // invoking the search dialog
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.marketplaceRecyclerView)

        // TODO: Set up marketplaceAdapter and marketplaceItem to uncomment this searchbar code
//        marketplaceAdapter = MarketplaceAdapter(marketplaceList)
//        recyclerView.adapter = marketplaceAdapter
//        listenForMarketplaceUpdates()
//
//        val searchHelper = SearchHelper(
//            context = this,
//            searchView = searchView,
//            recyclerView = recyclerView,
//            adapter = marketplaceAdapter,
//            dataList = marketplaceList,
//            filterFunction = ::filterMarketplaceItems  // Pass filter function to the helper
//        )
    }

//    // filters marketplace items based on the query
//    private fun filterMarketplaceItems(query: String): List<MarketplaceItem> {
//        return marketplaceList.filter { item ->
//            item.itemName.contains(query, ignoreCase = true) ||
//                    item.description.contains(query, ignoreCase = true)
//        }
//    }
//
//    private fun listenForMarketplaceUpdates() {
//        fun listenForEventsUpdate() {
//            db.collection("marketplace")
//                .addSnapshotListener { snapshot, e ->
//                    if (e != null) {
//                        Toast.makeText(this, "Error fetching marketplace items", Toast.LENGTH_SHORT)
//                            .show()
//                        return@addSnapshotListener
//                    }
//                    val updatedMarketplaceItem = mutableListOf<MarketplaceItem>()
//                    for (document in snapshot!!.documents) {
//                        val marketplaceItem = document.toObject(MarketplaceItem::class.java)
//                        if (marketplaceItem != null) {
//                            updatedMarketplaceItems.add(marketplaceItem)
//                        }
//                    }
//                    marketplaceAdapter.updateEvents(updatedMarketplaceItems)
//                }
//        }
//    }
}
