package com.example.gustiehub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_EMAIL = "com.example.gustiehub.email"

        fun newIntent(packageContext: Context, email: String): Intent? {
            return Intent(packageContext, DashboardActivity::class.java).apply {
                putExtra(EXTRA_EMAIL, email)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ExampleFragment())
                .commit()
        }

        val fragment = ExampleFragment()
        val email = intent.getStringExtra(EXTRA_EMAIL)
        // use the email to fetch information

        //initialize buttons reference
        val announcementsButton:Button = findViewById(R.id.see_all_announcements_button)
        val activityButton:Button = findViewById(R.id.see_all_activity_button)
        val groupsButton:ImageButton = findViewById(R.id.groups_button)
        val marketplaceButton:ImageButton = findViewById(R.id.marketplace_button)
        val eventsButton:ImageButton = findViewById(R.id.events_button)
        val messageButton:ImageView = fragment.view?.findViewById<ImageView>(R.id.messaging) ?: return
        val profileButton:ImageView = fragment.view?.findViewById<ImageView>(R.id.profile) ?: return
        val menuButton:ImageView = fragment.view?.findViewById<ImageView>(R.id.menu) ?: return

        //handling clicks for buttons.
        messageButton.setOnClickListener{
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener{
            val intent= Intent(this,ProfileActivity::class.java)
            startActivity(intent)
        }
        groupsButton.setOnClickListener {
            val intent = Intent(this, GroupsActivity::class.java)
            startActivity(intent)
        }
        marketplaceButton.setOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
            startActivity(intent)
        }
        menuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
        announcementsButton.setOnClickListener {
            val intent = Intent(this, AnnouncementsActivity::class.java)
            startActivity(intent)
        }
        activityButton.setOnClickListener {
            val intent = Intent(this,SeeAllActivity::class.java)
            startActivity(intent)
        }
        eventsButton.setOnClickListener {
            val intent = Intent(this, EventsActivity::class.java)
            startActivity(intent)
        }

    }

}