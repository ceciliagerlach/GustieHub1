package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()

    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val receivingId = intent.getStringExtra("userId")
        val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // get profile views
        val profileName: TextView = findViewById(R.id.userName)
        val profileYear: TextView = findViewById(R.id.classYear)
        val profileState: TextView = findViewById(R.id.homeState)
        val profileAreas: TextView = findViewById(R.id.areasOfStudy)
        val profileGroups: TextView = findViewById(R.id.joinedGroups)
        val profileImageView: ImageView = findViewById(R.id.profileImage)

        // editing profile views
        val editUserName: TextView = findViewById(R.id.editUserName)
        val editClassYear: TextView = findViewById(R.id.editClassYear)
        val editHomeState: TextView = findViewById(R.id.editHomeState)
        val editAreasOfStudy: TextView = findViewById(R.id.editAreasOfStudy)
        val btnEdit: Button = findViewById(R.id.btnEdit)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        // set user information
        if (receivingId.isNullOrEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstname = document.getString("firstName") ?: "No Name"
                        val lastname = document.getString("lastName") ?: "No Name"
                        val year = document.getLong("gradYear") ?: "No Grad Year"
                        val state = document.getString("homeState") ?: "No State"
                        val areas = document.getString("areasOfStudy") ?: "No Areas"
                        val groups = document.get("joinedGroups") as? List<String> ?: emptyList()

                        profileName.text = firstname + " " + lastname
                        profileYear.text = year.toString()
                        profileState.text = state
                        profileAreas.text = areas
                        profileGroups.text = groups.joinToString(", ")
                        
                        // load profile picture if it exists
                        val profilePictureUrl = document.getString("profilePicture")
                        if (!profilePictureUrl.isNullOrEmpty()) {
                          Glide.with(this)
                            .load(profilePictureUrl)  // URL from Firestore
                            .into(profileImageView)  // set the ImageView with the loaded image
                        } else {
                          // set a default image if no profile picture is available
                          profileImageView.setImageResource(R.drawable.sample_profile_picture)
                        }
                    }
                }
        } else {
            db.collection("users").document(receivingId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstname = document.getString("firstName") ?: "No Name"
                        val lastname = document.getString("lastName") ?: "No Name"
                        val year = document.getLong("gradYear") ?: "No Grad Year"
                        val state = document.getString("homeState") ?: "No State"
                        val areas = document.getString("areasOfStudy") ?: "No Areas"
                        val groups = document.get("joinedGroups") as? List<String> ?: emptyList()

                        profileName.text = firstname + " " + lastname
                        profileYear.text = year.toString()
                        profileState.text = state
                        profileAreas.text = areas
                        profileGroups.text = groups.joinToString(", ")

                        // load profile picture if it exists
                        val profilePictureUrl = document.getString("profilePicture")
                        if (!profilePictureUrl.isNullOrEmpty()) {
                          Glide.with(this)
                            .load(profilePictureUrl)  // URL from Firestore
                            .into(profileImageView)  // set the ImageView with the loaded image
                        } else {
                          // set a default image if no profile picture is available
                          profileImageView.setImageResource(R.drawable.sample_profile_picture)
                    }
                }
            }
        }

        //function for listening for edits and saves
        btnEdit.setOnClickListener {
            editUserName.text = profileName.text
            editClassYear.text = profileYear.text
            editHomeState.text = profileState.text
            editAreasOfStudy.text = profileAreas.text

            editUserName.visibility = TextView.VISIBLE
            editClassYear.visibility = TextView.VISIBLE
            editHomeState.visibility = TextView.VISIBLE
            editAreasOfStudy.visibility = TextView.VISIBLE


            profileName.visibility = TextView.GONE
            profileYear.visibility = TextView.GONE
            profileState.visibility = TextView.GONE
            profileAreas.visibility = TextView.GONE

            btnEdit.visibility = Button.GONE
            btnCancel.visibility = Button.VISIBLE
            btnSave.visibility = Button.VISIBLE

        }

        btnCancel.setOnClickListener {
            editUserName.visibility = TextView.GONE
            editClassYear.visibility = TextView.GONE
            editHomeState.visibility = TextView.GONE
            editAreasOfStudy.visibility = TextView.GONE

            btnEdit.visibility = Button.VISIBLE
            btnCancel.visibility = Button.GONE
            btnSave.visibility = Button.GONE

            profileName.visibility = TextView.VISIBLE
            profileYear.visibility = TextView.VISIBLE
            profileState.visibility = TextView.VISIBLE
            profileAreas.visibility = TextView.VISIBLE

        }

        btnSave.setOnClickListener {
            profileName.text = editUserName.text
            profileYear.text = editClassYear.text
            profileState.text = editHomeState.text
            profileAreas.text = editAreasOfStudy.text

            editUserName.visibility = TextView.GONE
            editClassYear.visibility = TextView.GONE
            editHomeState.visibility = TextView.GONE
            editAreasOfStudy.visibility = TextView.GONE

            btnEdit.visibility = Button.VISIBLE
            btnCancel.visibility = Button.GONE
            btnSave.visibility = Button.GONE

            profileName.visibility = TextView.VISIBLE
            profileYear.visibility = TextView.VISIBLE
            profileState.visibility = TextView.VISIBLE
            profileAreas.visibility = TextView.VISIBLE

            // update user profile in Firestore
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userId = it.uid
                val userRef = db.collection("users").document(userId)
                userRef.update(
                    mapOf(
                        "fullName" to editUserName.text.toString(),
                        "gradYear" to editClassYear.text.toString().toIntOrNull(),
                        "homeState" to editHomeState.text.toString(),
                        "areasOfStudy" to editAreasOfStudy.text.toString()
                    )
                )
                    .addOnSuccessListener {
                        Log.d("ProfileActivity", "User profile updated successfully")
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileActivity", "Error updating user profile", e)
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
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
        GlobalData.getFilteredGroupList(userId) { updatedGroups ->
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

        // handling clicks for message button
        val messageButton: ImageView = findViewById(R.id.messaging)
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
    }
}
