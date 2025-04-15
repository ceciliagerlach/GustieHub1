package com.example.gustiehub

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.SearchView

class SearchHelper<T>(
    private val context: Context,
    private val searchView: SearchView,
    private val recyclerView: RecyclerView,
    private val adapter: RecyclerView.Adapter<*>,
    private var dataList: MutableList<T>,
    private val filterFunction: (String) -> List<T>,
    private val updateFunction: (List<T>) -> Unit
) {
    init {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filtered = filterFunction(query ?: "")
                updateFunction(filtered)
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = filterFunction(newText ?: "")
                updateFunction(filtered)
                return true
            }

            private fun hideKeyboard() {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
            }
        })
    }

    fun updateDataList(newList: MutableList<T>) {
        dataList = newList
    }
}

