package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.gustiehub.GlobalData.groupList
import com.google.android.material.navigation.NavigationView

class GroupsActivity : AppCompatActivity() {
    // variables for recycler view, displaying list of groups
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var menuAdapter: MenuAdapter
    private val groupsNameList = mutableListOf<String>()
//    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private val db = FirebaseFirestore.getInstance()

    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    // variables for search
    private val allGroupsList = mutableListOf<Group>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        val createGroupButton = findViewById<ImageButton>(R.id.create_groups_button)

        // display list of groups
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        groupsAdapter = GroupsAdapter(filteredGroupList, onItemClick = { selectedGroup ->
            val intent = Intent(this, GroupsActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        },
        onJoinGroupClick = { selectedGroup ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userId = user.uid
                val currentUser =
                    User(userId, user.email ?: "", "", "", _gradYear = 0, "", _areasOfStudy = "")
                currentUser.joinGroup(selectedGroup.name)
                Toast.makeText(this, "Joined group: ${selectedGroup.name}", Toast.LENGTH_SHORT)
                    .show()
                GlobalData.getGroupList(userId) { updatedGroups ->
                    runOnUiThread {
                        groupList.clear()
                        groupList.addAll(updatedGroups)
                        filteredGroupList.clear()
                        filteredGroupList.addAll(updatedGroups)
                        groupsAdapter.notifyDataSetChanged()
                    }
                }
            } else run {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
                }
        } )
        groupsRecyclerView.adapter = groupsAdapter

        createGroupButton.setOnClickListener {
            NewGroupDialog()
        }

        val userID = FirebaseAuth.getInstance().currentUser?.uid
        groupsRecyclerView.adapter = groupsAdapter
        if (userID != null) {
            GlobalData.getGroupList(userID) { updatedGroups ->
                runOnUiThread {
                    groupList.clear()
                    groupList.addAll(updatedGroups)
                    filteredGroupList.clear()
                    filteredGroupList.addAll(updatedGroups)
                    groupsAdapter.notifyDataSetChanged()
                }
            }
            GlobalData.getFilteredGroupList(userID) { updatedGroups ->
                runOnUiThread {
                    menuAdapter.updateGroups(updatedGroups)
                }
            }
        }

        // list of groups in tab
        menuRecyclerView = findViewById(R.id.recycler_menu)
        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuAdapter = MenuAdapter(filteredGroupList) { selectedGroup ->
            val intent = Intent(this, GroupPageActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        }
        menuRecyclerView.adapter = menuAdapter
//        if (userID != null) {
//            GlobalData.getFilteredGroupList(userID){ updatedGroups ->
//                runOnUiThread {
//                    groupList.clear()
//                    groupList.addAll(updatedGroups)
//                    menuAdapter.updateGroups(updatedGroups)
//                }
//            }
//        }

        //set up drawer layout and handle clicks for menu items
        drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
        navView = findViewById<NavigationView>(R.id.nav_view)
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
                    drawerLayout.closeDrawer(GravityCompat.START)
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

        // search functionality
        val searchView: SearchView = findViewById(R.id.searchView)
        val recyclerView: RecyclerView = findViewById(R.id.groupsRecyclerView)
//        listenForGroupsUpdate()

        val searchHelper = SearchHelper(
            context = this,
            searchView = searchView,
            recyclerView = recyclerView,
            adapter = groupsAdapter,
            dataList = groupList, // master list
            filterFunction = ::filterGroups,
            updateFunction = { filtered ->
                filteredGroupList.clear()
                filteredGroupList.addAll(filtered)
                groupsAdapter.notifyDataSetChanged()
            }
        )
    }

//    private fun listenForGroupsUpdate() {
//        db.collection("groups")
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    Toast.makeText(this, "Error fetching groups", Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//
//                groupList.clear()
//                filteredGroupList.clear()
//
//                for (document in snapshot!!.documents) {
//                    val group = document.toObject(Group::class.java)
//                    if (group != null && !group.name.contains("Class of") &&!group.name.equals("Gusties")) {
//                        groupList.add(group)
//                    }
//                }
//
//                filteredGroupList.addAll(groupList)
//                groupsAdapter.notifyDataSetChanged()
//            }
//    }

    private fun filterGroups(query: String): List<Group> {
        if (query.isBlank()) return groupList
        return groupList.filter { it.name.contains(query, ignoreCase = true) }
    }




    private fun NewGroupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_group_dialog, null)
        val editTextGroupName = dialogView.findViewById<EditText>(R.id.newGroupName)
        val editTextGroupDescription = dialogView.findViewById<EditText>(R.id.newGroupDescription)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonConfirm.setOnClickListener {
            val groupName = editTextGroupName.text.toString()
            val groupDescription = editTextGroupDescription.text.toString()
            if (groupName.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid
                    val group = Group(groupName, userId,description = groupDescription)
                    group.createGroup()
                    val currentUser = User(userId, user.email ?: "", "", "", _gradYear = 0, _homeState = "", _areasOfStudy = "")
                    currentUser.joinGroup(groupName)
                    Toast.makeText(this, "Group Created: $groupName", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
                }
            } else {
                editTextGroupName.error = "Group name cannot be empty"
            }
        }
        dialog.show()
    }

}

