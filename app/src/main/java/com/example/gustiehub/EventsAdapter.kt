package com.example.gustiehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventsAdapter(
    private var events: MutableList<Event>)
    : RecyclerView.Adapter<EventsAdapter.EventsViewHolder>() {

    class EventsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.event_date)
        val timeTextView: TextView = itemView.findViewById(R.id.event_time)
        val locationTextView: TextView = itemView.findViewById(R.id.event_location)
        val nameTextView: TextView = itemView.findViewById(R.id.event_name)
        val descriptionTextView: TextView = itemView.findViewById(R.id.event_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventsViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val event = events[position]
        holder.dateTextView.text = event.date
        holder.timeTextView.text = event.time
        holder.locationTextView.text = event.location
        holder.nameTextView.text = event.eventName
        holder.descriptionTextView.text = event.text
    }

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = events.size
}
