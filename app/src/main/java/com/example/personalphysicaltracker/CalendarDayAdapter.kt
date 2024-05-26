package com.example.personalphysicaltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.Activity
import java.time.LocalDate

class CalendarDayAdapter(
    private var activities: List<Activity>,
    private var activitiesList: List<ActivitiesList>,
    private var selectedDate: LocalDate //yyyy-MM-dd
) : RecyclerView.Adapter<CalendarDayAdapter.MyViewHolder>() {
    //private var activities = emptyList<Activity>()
    private val activityIdToNameMap: Map<Int, String> =
        activitiesList.associate { it.id to it.name }

    private var activitiesOfThisDay = getDayActivities(activities, selectedDate)


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return CalendarDayAdapter.MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_activity_day_calendar_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return activitiesOfThisDay.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = activitiesOfThisDay[position]

        holder.itemView.visibility = View.VISIBLE

        val idTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_day_calendar_row)
        val nameTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_day_calendar_row)
        val activityName = activityIdToNameMap[currentitem.activityId]
        val tcTime =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tc_time_day_calendar_row)


        idTextView.text = currentitem.id.toString()
        nameTextView.text = activityName ?: "Unknown Activity"
        var startTimeString = ""
        var endTimeString = ""
        //check if the activity starts on the they before
        val activityStartDate = currentitem.startTime
        val selectedDateStartMillis =
            selectedDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        if (activityStartDate < selectedDateStartMillis) {
            startTimeString = buildString {
                append(selectedDate.minusDays(1).dayOfWeek.toString().substring(0, 3))
                append(" ")
                append(timeStringFromLong(currentitem.startTime, true))
            }
        } else {
            startTimeString = timeStringFromLong(currentitem.startTime, true)
        }

        //check if the activity ends on the day after
        val activityEndDate = currentitem.stopTime
        val selectedDateEndMillis =
            selectedDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                .toEpochMilli()
        if (activityEndDate > selectedDateEndMillis) {
            endTimeString = buildString {
                //append(selectedDate.plusDays(1).dayOfWeek.toString().substring(0, 3))
                //append(" ")
                append(timeStringFromLong(currentitem.stopTime, true))
                append(" ")
                append(selectedDate.plusDays(1).dayOfWeek.toString().substring(0, 3))
            }
        } else {
            endTimeString = timeStringFromLong(currentitem.stopTime, true)
        }

        tcTime.text = buildString {
            append(startTimeString)
            append(" - ")
            append(endTimeString)
        }

    }

    fun updateData(
        newSelectedDate: LocalDate
    ) {
        selectedDate = newSelectedDate

        activitiesOfThisDay = getDayActivities(activities, selectedDate)

        notifyDataSetChanged()
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