package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    private val db = FirebaseFirestore.getInstance()

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

        //firebase function for listening from firebase
        listenForEventsUpdate()
        val createEventButton = findViewById<ImageButton>(R.id.create_events_button)
        createEventButton.setOnClickListener {
            NewEventsDialog()
        }


    }

    private fun listenForEventsUpdate() {
        db.collection("events")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error fetching events", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                eventsList.clear()
                for (document in snapshot!!.documents) {
                    val event = document.toObject(Event::class.java)
                    if (event != null) {
                        eventsList.add(event)
                    }
                }
                eventsAdapter.notifyDataSetChanged()
            }
    }


    private fun NewEventsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_event_dialog, null)
        val editTextEventName = dialogView.findViewById<EditText>(R.id.newEventName)
        val editTextEventDescription = dialogView.findViewById<EditText>(R.id.newEventDescription)
        val editTextEventLocation = dialogView.findViewById<EditText>(R.id.newEventLocation)
        val editTextEventDate = dialogView.findViewById<EditText>(R.id.newEventDate)
        val editTextEventTime = dialogView.findViewById<EditText>(R.id.newEventTime)
        val editTextEventGroup = dialogView.findViewById<EditText>(R.id.newEventGroup)
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
            val eventName = editTextEventName.text.toString()
            val eventDescription = editTextEventDescription.text.toString()
            val eventLocation = editTextEventLocation.text.toString()
            val eventDate = editTextEventDate.text.toString()
            val eventTime = editTextEventTime.text.toString()
            val eventGroup = editTextEventGroup.text.toString()
            if (eventName.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid
                    val event = Event(userId, eventName, eventGroup, eventDescription, eventTime, eventLocation, eventDate)
                    event.createEvent()
                    Toast.makeText(this, "Event Created: $eventName", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
                }
            } else {
                editTextEventName.error = "Event name cannot be empty"
            }
        }
        dialog.show()
    }

}
