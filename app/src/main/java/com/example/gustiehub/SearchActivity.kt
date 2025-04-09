package com.example.gustiehub

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SearchActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                doMySearch(query)
            }
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