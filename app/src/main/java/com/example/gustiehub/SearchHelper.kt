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
    private val dataList: MutableList<T>,
    private val filterFunction: (String) -> List<T>,
    private val updateFunction: (List<T>) -> Unit
) {

    init {
        setUpSearchListener()
    }

    private fun setUpSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    filterData(it)
                }
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterData(query: String) {
        val filteredList = filterFunction(query)
        updateFunction(filteredList)
    }

    private fun hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchView.windowToken, 0)
    }
}
