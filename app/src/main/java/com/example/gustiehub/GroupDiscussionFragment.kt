package com.example.gustiehub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupDiscussionFragment(val groupName: String) : Fragment() {
    // variables for recycler view, displaying list of groups and posts
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsAdapter: PostAdapter
    private val postList = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_discussion_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // list of posts on group page
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        postsRecyclerView.layoutManager = LinearLayoutManager(activity)
        postsAdapter = PostAdapter(postList)
        postsRecyclerView.adapter = postsAdapter
        GlobalData.getPosts(groupName) { updatedPosts ->
            requireActivity().runOnUiThread {
                postsAdapter.updatePosts(updatedPosts)
            }
        }
    }

}