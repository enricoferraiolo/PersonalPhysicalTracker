package com.example.personalphysicaltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextClock
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.ui.manageActivitylist.ActivitiesListAdapter

class CalendarDayAdapter(
    private val activities: List<Activity>,
    private val activitiesList: List<ActivitiesList>
) : RecyclerView.Adapter<CalendarDayAdapter.MyViewHolder>() {
    //private var activities = emptyList<Activity>()
    private val activityIdToNameMap: Map<Int, String> =
        activitiesList.associate { it.id to it.name }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return CalendarDayAdapter.MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_activity_day_calendar_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return activities.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = activities[position]
        val idTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_day_calendar_row)
        idTextView.text = currentitem.id.toString()
        val nameTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_day_calendar_row)
        val activityName = activityIdToNameMap[currentitem.activityId]
        nameTextView.text = activityName ?: "Unknown Activity"
        val tcStart =
            holder.itemView.findViewById<TextView>(R.id.tc_start_time_day_calendar_row)
        val tcEnd =
            holder.itemView.findViewById<TextView>(R.id.tc_end_time_day_calendar_row)

        tcStart.text = timeStringFromLong(currentitem.startTime, true)
        tcEnd.text = timeStringFromLong(currentitem.stopTime, true)

    }

    private fun timeStringFromLong(elapsedTimeMillis: Long, showSeconds: Boolean): String {
        val seconds = (elapsedTimeMillis / 1000) % 60
        val minutes = (elapsedTimeMillis / (1000 * 60) % 60)
        val hours = (elapsedTimeMillis / (1000 * 60 * 60) % 24)
        return makeTimeString(hours, minutes, seconds, showSeconds)
    }

    private fun makeTimeString(
        hours: Long,
        minutes: Long,
        seconds: Long,
        showSeconds: Boolean
    ): String {
        return if (showSeconds) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", hours, minutes)
        }
    }

}