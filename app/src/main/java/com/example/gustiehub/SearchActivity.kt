package com.example.gustiehub

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class SearchActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private val searchResults = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.recycler_search)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventsAdapter(searchResults)
        recyclerView.adapter = adapter

        val query = intent.getStringExtra("query") ?: ""
        searchEvents(query)
    }

    private fun searchEvents(query: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .get()
            .addOnSuccessListener { snapshot ->
                searchResults.clear()
                for (doc in snapshot.documents) {
                    val event = doc.toObject(Event::class.java)
                    if (event != null && (
                                event.eventName.contains(query, ignoreCase = true) ||
                                        event.text.contains(query, ignoreCase = true)
                                )) {
                        searchResults.add(event)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    //TODO: implement actual search functionality
    //TODO: could maybe somehow ties this into the adapter, so I don't have to duplicate all the views

    //If your data is stored in a SQLite database on the device,
    //performing a full-text search—using FTS3, rather than a LIKE query
    //can provide a more robust search across text data and can produce results significantly faster.
    //See sqlite.org for information about FTS3 and the SQLiteDatabase class for information about SQLite on Android.
    private fun doMySearch(query: String) {

    }

}