package com.example.gustiehub

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity, groupName: String) :
    FragmentStateAdapter(fragmentActivity) {
        val groupName = groupName

    override fun getItemCount(): Int = 2 // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GroupDiscussionFragment(groupName)
            1 -> GroupInformationFragment(groupName)
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}