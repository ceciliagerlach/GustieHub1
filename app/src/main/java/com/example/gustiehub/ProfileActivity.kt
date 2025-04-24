package com.example.gustiehub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
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
    // Variables for recycler views
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()

    // Variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val receivingId = intent.getStringExtra("userId")
        val userId: String = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // Get profile views
        val profileName: TextView = findViewById(R.id.userName)
        val profileYear: TextView = findViewById(R.id.classYear)
        val profileState: TextView = findViewById(R.id.homeState)
        val profileAreas: TextView = findViewById(R.id.areasOfStudy)
        val profileGroups: TextView = findViewById(R.id.joinedGroups)
        val profileImageView: ImageView = findViewById(R.id.profileImage)

        // Editing profile views
        val editAreasOfStudy: TextView = findViewById(R.id.editAreasOfStudy)
        val view: View = findViewById(R.id.view)
        val btnEdit: ImageButton = findViewById(R.id.btnEdit)
        val btnCancel: Button = findViewById(R.id.btnCancel)
        val btnSave: Button = findViewById(R.id.btnSave)

        // Set user information
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
            btnEdit.visibility = Button.GONE
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

        // Handle button listening for edits and saves
        btnEdit.setOnClickListener {
            editAreasOfStudy.text = profileAreas.text
            editAreasOfStudy.visibility = TextView.VISIBLE

            profileAreas.visibility = TextView.GONE
            view.visibility = View.GONE

            btnEdit.visibility = Button.GONE
            btnCancel.visibility = Button.VISIBLE
            btnSave.visibility = Button.VISIBLE
        }

        btnCancel.setOnClickListener {
            editAreasOfStudy.visibility = TextView.GONE

            btnEdit.visibility = Button.VISIBLE
            btnCancel.visibility = Button.GONE
            btnSave.visibility = Button.GONE

            profileAreas.visibility = TextView.VISIBLE
            view.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            profileAreas.text = editAreasOfStudy.text

            editAreasOfStudy.visibility = TextView.GONE

            btnEdit.visibility = Button.VISIBLE
            btnCancel.visibility = Button.GONE
            btnSave.visibility = Button.GONE

            profileAreas.visibility = TextView.VISIBLE
            view.visibility = View.VISIBLE

            // Update user profile in Firestore
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val userId = it.uid
                val userRef = db.collection("users").document(userId)
                userRef.update(
                    mapOf(
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

        // List of groups in tab
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

        // Set up drawer layout and handle clicks for menu items
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

        // Opening menu
        val menuButton: ImageView = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handling clicks for message and profile button
        val messageButton: ImageView = findViewById(R.id.messaging)
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        val profileButton: ImageView = findViewById(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
