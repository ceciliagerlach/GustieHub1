package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gustiehub.GroupsAdapter.GroupViewHolder

class AnnouncementAdapter (
    private var announcementList: List<Announcement>
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTextView: TextView = itemView.findViewById(R.id.header_text)
        val announcementTextView: TextView = itemView.findViewById(R.id.announcement_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.announcement_item, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcementList[position]
        holder.headerTextView.text = announcement.header
        holder.announcementTextView.text = announcement.text
    }

    fun updateAnnouncements(newAnnouncements: List<Announcement>) {
        announcementList = newAnnouncements
        notifyDataSetChanged()
    }

    override fun getItemCount() = announcementList.size

}