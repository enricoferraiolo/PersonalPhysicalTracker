package com.example.personalphysicaltracker

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.data.ExtraInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate

class CalendarDayAdapter(
    private var activities: List<Activity>,
    private var activitiesList: List<ActivitiesList>,
    private var selectedDate: LocalDate //yyyy-MM-dd
) : RecyclerView.Adapter<CalendarDayAdapter.MyViewHolder>() {
    //private var activities = emptyList<Activity>()
    private val activityIdToNameMap: Map<Int?, String> =
        activitiesList.associate { it.id to it.name }.plus(null to "Unknown activity")

    val dayActivities: List<Activity> = getDayActivities(activities, selectedDate)
    private var activitiesOfThisDay = fillWithDummyActivities(dayActivities)

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
        //fill activitiesOfThisDay with dummy
        val currentitem = activitiesOfThisDay[position]

        holder.itemView.visibility = View.VISIBLE

       // val idTextView =
           // holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_day_calendar_row)
        val nameTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_day_calendar_row)
        val activityName = activityIdToNameMap[currentitem.activityId]
        val tcTime =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tc_time_day_calendar_row)


       // idTextView.text = currentitem.id.toString()

        if (activityName != null) {
            nameTextView.text = activityName
            nameTextView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
            ) // Normal font for known activities
        } else {
            nameTextView.text = holder.itemView.context.getString(R.string.dummy_activity)
            nameTextView.setTypeface(
                null,
                android.graphics.Typeface.ITALIC
            ) // Italic font for unknown activities

        }

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
        if (currentitem.id < 0) {
            tcTime.setTypeface(
                android.graphics.Typeface.MONOSPACE,
                android.graphics.Typeface.ITALIC
            )
        } else {
            tcTime.setTypeface(
                android.graphics.Typeface.MONOSPACE,
                android.graphics.Typeface.BOLD
            )
        }

        // Set click listener to show a Toast with the activity name
        holder.itemView.setOnClickListener {
            val context: Context = it.context
            Toast.makeText(context, "Activity: ${nameTextView.text}", Toast.LENGTH_SHORT).show()
        }
    }



    /*
    every day must be covered with activities, if there are no activities, fill with a dummy activity that has a start time of 00:00:00 and an end time of 23:59:59
    if there is an activity that starts and end on the same day, then one dummy activity will cover the part of the day before the activity and another dummy activity will cover the part of the day after the activity
     */
    //FIXME: controlla cosa succede se attività e messa nel giorno prima/dopo
    private fun fillWithDummyActivities(activitiesOfThisDay: List<Activity>): List<Activity> {
        val dummyActivities = mutableListOf<Activity>()

        // Check if there are no activities for the selected day
        if (activitiesOfThisDay.isEmpty()) {
            Log.d("CalendarDayAdapter", "No activities for the selected day")
            // Create a dummy activity covering the whole day
            val dummyActivity = Activity(
                id = -1, // Assign a unique negative id for dummy activities
                userId = -1, // Assuming -1 represents dummy user in your system
                activityId = -1, // Assuming -1 represents dummy activity in your system
                startTime = selectedDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                    .toEpochMilli(), // Start time at 00:00:00
                stopTime = selectedDate.plusDays(1).atStartOfDay().minusSeconds(1)
                    .toInstant(java.time.ZoneOffset.UTC).toEpochMilli(), // End time at 23:59:59
                extra = ExtraInfo(
                    stepsSelector = false,
                    metersSelector = false,
                    steps = null,
                    meters = null
                )
            )
            dummyActivities.add(dummyActivity)
        } else {
            // Sort activities by start time
            val sortedActivities = activitiesOfThisDay.sortedBy { it.startTime }

            // Check if the first activity starts after midnight
            if (sortedActivities.first().startTime > selectedDate.atStartOfDay()
                    .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            ) {
                // Create a dummy activity covering the time before the first activity
                val dummyActivityBefore = Activity(
                    id = -2, // Assign another unique negative id for dummy activities
                    userId = -1, // Assuming -1 represents dummy user in your system
                    activityId = -1, // Assuming -1 represents dummy activity in your system
                    startTime = selectedDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                        .toEpochMilli(), // Start time at 00:00:00
                    stopTime = sortedActivities.first().startTime - 1000, // End time before the first activity starts
                    extra = ExtraInfo(
                        stepsSelector = false,
                        metersSelector = false,
                        steps = null,
                        meters = null
                    )
                )
                dummyActivities.add(dummyActivityBefore)
            }

            // Check for gaps between activities and add dummy activities
            for (i in 0 until sortedActivities.size - 1) {
                val currentActivity = sortedActivities[i]
                val nextActivity = sortedActivities[i + 1]

                if (currentActivity.stopTime < nextActivity.startTime - 1000) {
                    val dummyActivityBetween = Activity(
                        id = -4,
                        userId = -1, // Assuming -1 represents dummy user in your system
                        activityId = -1, // Assuming -1 represents dummy activity in your system
                        startTime = currentActivity.stopTime + 1000, // Start time one second after the current activity ends
                        stopTime = nextActivity.startTime - 1000, // End time one second before the next activity starts
                        extra = ExtraInfo(
                            stepsSelector = false,
                            metersSelector = false,
                            steps = null,
                            meters = null
                        )
                    )
                    dummyActivities.add(dummyActivityBetween)
                }
            }

            // Check if the last activity ends before midnight
            if (sortedActivities.last().stopTime < selectedDate.plusDays(1).atStartOfDay()
                    .toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            ) {
                // Create a dummy activity covering the time after the last activity ends
                val dummyActivityAfter = Activity(
                    id = -3, // Assign another unique negative id for dummy activities
                    userId = -1, // Assuming -1 represents dummy user in your system
                    activityId = -1, // Assuming -1 represents dummy activity in your system
                    startTime = sortedActivities.last().stopTime + 1000, // Start time after the last activity ends
                    stopTime = selectedDate.plusDays(1).atStartOfDay()
                        .toInstant(java.time.ZoneOffset.UTC).minusSeconds(1)
                        .toEpochMilli(), // End time at 23:59:59
                    extra = ExtraInfo(
                        stepsSelector = false,
                        metersSelector = false,
                        steps = null,
                        meters = null
                    )
                )
                dummyActivities.add(dummyActivityAfter)
            }
        }


        // Combine dummy activities with actual activities
        val combinedList = mutableListOf<Activity>()
        combinedList.addAll(activitiesOfThisDay)
        combinedList.addAll(dummyActivities)

        return combinedList.sortedBy { it.startTime }
    }


    fun updateData(
        newSelectedDate: LocalDate
    ) {
        selectedDate = newSelectedDate
        val dayActivities: List<Activity> = getDayActivities(activities, selectedDate)
        activitiesOfThisDay = fillWithDummyActivities(dayActivities)

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