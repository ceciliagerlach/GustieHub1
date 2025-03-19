package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity() {
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

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

        val email = intent.getStringExtra(EXTRA_EMAIL)

        //set up drawer layout and handle clicks for menu items
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
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        //initialize buttons reference
        val messageButton: ImageView = findViewById(R.id.messaging)
        val profileButton: ImageView = findViewById(R.id.profile)
        val menuButton: ImageView = findViewById(R.id.menu)
        val announcementsButton: Button = findViewById(R.id.see_all_announcements_button)
        val activityButton: Button = findViewById(R.id.see_all_activity_button)
        val myGroupsButton: ImageButton = findViewById(R.id.my_groups_button)
        val marketplaceButton: ImageButton = findViewById(R.id.marketplace_button)
        val eventsButton: ImageButton = findViewById(R.id.events_button)

        //handling clicks for buttons
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        myGroupsButton.setOnClickListener {
            val intent = Intent(this, GroupsActivity::class.java)
            startActivity(intent)
        }
        marketplaceButton.setOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
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
        activityButton.setOnClickListener {
            val intent = Intent(this, SeeAllActivity::class.java)
            startActivity(intent)
        }
        eventsButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            startActivity(intent)
        }
    }
}