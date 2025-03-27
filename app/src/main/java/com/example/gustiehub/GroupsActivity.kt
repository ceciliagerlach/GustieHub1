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
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private val db = FirebaseFirestore.getInstance()
    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        val createGroupButton = findViewById<ImageButton>(R.id.create_groups_button)

        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
        groupsAdapter = GroupsAdapter(groupList, onItemClick = { selectedGroup ->
            val intent = Intent(this, GroupPageActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        },
        onJoinGroupClick = { selectedGroup ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userId = user.uid
                val currentUser = User(userId, user.email ?: "", "", "", _gradYear = 0, "", _areasOfStudy = "")
                currentUser.joinGroup(selectedGroup.name)
                Toast.makeText(this, "Joined group: ${selectedGroup.name}", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
            }

        } )
        groupsRecyclerView.adapter = groupsAdapter

        //firebase function for listening from firebase
        listenForGroupsUpdates()

        createGroupButton.setOnClickListener {
            NewGroupDialog()
        }

        // list in discover groups
//        groupsRecyclerView = findViewById(R.id.groupsRecyclerView)
//        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
//        groupsAdapter = GroupsAdapter(groupList,) { selectedGroup ->
//            val intent = Intent(this, GroupsActivity::class.java)
//            intent.putExtra("groupName", selectedGroup.name)
//            startActivity(intent)
//        }
        groupsRecyclerView.adapter = groupsAdapter
        GlobalData.getGroupList { updatedGroups ->
            runOnUiThread {
                groupList.clear()
                groupList.addAll(updatedGroups)
                groupsAdapter.updateGroups(updatedGroups)
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
    }

    private fun listenForGroupsUpdates() {
        db.collection("groups")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error fetching groups", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                groupsNameList.clear()
                for (document in snapshot!!.documents) {
                    val groupName = document.getString("name")
                    if (groupName != null) {
                        groupsNameList.add(groupName)
                    }
                }
                groupsAdapter.notifyDataSetChanged()
            }
    }

    private fun NewGroupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_group_dialog, null)
        val editTextGroupName = dialogView.findViewById<EditText>(R.id.newGroupName)
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
            if (groupName.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid
                    val group = Group(groupName, userId)
//                    group.createGroup()
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

