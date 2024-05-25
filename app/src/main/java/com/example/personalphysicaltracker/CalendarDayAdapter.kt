package com.example.personalphysicaltracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.Activity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CalendarDayAdapter(
    private val activities: List<Activity>,
    private val activitiesList: List<ActivitiesList>,
    private var selectedDate: LocalDate //yyyy-MM-dd
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

        holder.itemView.visibility = View.VISIBLE

        val idTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_day_calendar_row)
        val nameTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_day_calendar_row)
        val activityName = activityIdToNameMap[currentitem.activityId]
        val tcStart =
            holder.itemView.findViewById<TextView>(R.id.tc_start_time_day_calendar_row)
        val tcEnd =
            holder.itemView.findViewById<TextView>(R.id.tc_end_time_day_calendar_row)

        //check if activity has been registered for the day
        //an activity is DISPLAYED IFF given day D and activity A, there is an entry in the database
        //s.t. A.startTime is in D and A.stopTime is in D OR A.startTime is in D-1 and A.stopTime is in D OR A.startTime is in D and A.stopTime is in D+1

// Check if activity has been registered for the day
        val activityStartDate = currentitem.startTime
        val activityEndDate = currentitem.stopTime

        val selectedDateStartMillis =
            selectedDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        val selectedDateEndMillis =
            selectedDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()

        val activityRegisteredForSelectedDay =
            (activityStartDate in selectedDateStartMillis until selectedDateEndMillis) ||
                    (activityEndDate in selectedDateStartMillis until selectedDateEndMillis)

        if (activityRegisteredForSelectedDay) {
            idTextView.text = currentitem.id.toString()
            nameTextView.text = activityName ?: "Unknown Activity"
            tcStart.text = timeStringFromLong(currentitem.startTime, true)
            tcEnd.text = timeStringFromLong(currentitem.stopTime, true)
        } else {
            holder.itemView.visibility =
                View.GONE // Hide the item if activity is not registered for the selected day
        }
    }

    fun updateSelectedDate(newDate: LocalDate) {
        selectedDate = newDate
        notifyDataSetChanged()
    }

    private fun getYear(time: Long): String {
        val date = java.time.Instant.ofEpochMilli(time)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return date.year.toString()
    }

    private fun getMonth(time: Long): String {
        val date = java.time.Instant.ofEpochMilli(time)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return date.monthValue.toString()
    }

    private fun getDay(time: Long): String {
        val date = java.time.Instant.ofEpochMilli(time)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return date.dayOfMonth.toString()
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