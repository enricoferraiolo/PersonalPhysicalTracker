package com.example.personalphysicaltracker

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class CalendarDayAdapter(
    private var activities: List<Activity>,
    private var activitiesList: List<ActivitiesList>,
    private var selectedDate: LocalDate //yyyy-MM-dd
) : RecyclerView.Adapter<CalendarDayAdapter.MyViewHolder>() {
    private var activitiesViewModel: ActivitiesViewModel =
        ActivitiesRepository.getActivitiesViewModel()!!

    private val activityIdToNameMap: Map<Int?, String> =
        activitiesList.associate { it.id to it.name }.plus(null to "Deleted activity")


    val dayActivities: List<Activity> = getDayActivities(
        activities,
        selectedDate,
        activitiesList //all'inizio tutte le attività sono nel filtro
    )

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
        val autoTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tv_auto_day_calendar_row)
        val nameTextView =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_day_calendar_row)
        val activityName = activityIdToNameMap[currentitem.activityId]
        val tcTime =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tc_time_day_calendar_row)
        val tvSteps =
            holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.tv_steps_day_calendar_row)
        val timeZone = currentitem.timeZone

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

        //check if currentItem is first item in day
        val isFirstItemInDay = position == 0
        //check if currentItem is last item in day
        val isLastItemInDay = position == activitiesOfThisDay.size - 1

        val startTimeString = formatTimeClockString(
            currentitem.startTime,
            selectedDate,
            true,
            timeZone,
            isFirstItemInDay = isFirstItemInDay
        )
        val endTimeString = formatTimeClockString(
            currentitem.stopTime,
            selectedDate,
            false,
            timeZone,
            isLastItemInDay = isLastItemInDay
        )

        Log.d("CalendarDayAdapter", "Item: ${currentitem.id} Zone: $timeZone")

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

        //tvSteps.text = currentitem.extra?.steps?.toString() ?: "N/A"
        if (currentitem.steps != null) {
            tvSteps.text = currentitem.steps.toString() + " \uD83D\uDC63"
        } else {
            tvSteps.text = ""
        }

        //auto detected activity
        if (currentitem.auto) {
            autoTextView.visibility = View.VISIBLE
        } else {
            autoTextView.visibility = View.GONE
        }

        // Set click listener to show a Toast with the activity name
        holder.itemView.setOnClickListener {
            val context: Context = it.context
            Toast.makeText(context, "Activity: ${nameTextView.text}", Toast.LENGTH_SHORT).show()
        }

        //set on long click listener to delete the activity
        holder.itemView.setOnLongClickListener {
            //check if the activity is a dummy activity
            if (currentitem.id < 0) {
                return@setOnLongClickListener false
            }

            val context: Context = it.context
            showDeleteConfirmationDialog(context, currentitem)
            true
        }
    }

    private fun showDeleteConfirmationDialog(context: Context, activity: Activity) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Delete Activity")
        builder.setMessage("Are you sure you want to delete this activity?")

        builder.setPositiveButton("OK") { dialog, which ->
            deleteActivity(activity)
            Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun deleteActivity(activity: Activity) {
        activitiesViewModel.deleteActivity(activity)
        activities = activities.filter { it.id != activity.id }
        activitiesOfThisDay =
            fillWithDummyActivities(getDayActivities(activities, selectedDate, activitiesList))
        notifyDataSetChanged()
    }


    fun formatTimeClockString(
        eventTimeMillis: Long,
        selectedDate: LocalDate,
        isStartTime: Boolean, // if true, check if activity started before selectedDate, if false, check if activity ended after selectedDate
        timeZone: String,
        isFirstItemInDay: Boolean = false,
        isLastItemInDay: Boolean = false
    ): String {
        // Handle time zone
        val savedZoneId = java.time.ZoneId.of(timeZone)
        val currentZoneId = java.time.ZoneId.systemDefault() // Current phone time zone

        // Convert event time to the current time zone
        val eventTimeInCurrentZone = java.time.Instant.ofEpochMilli(eventTimeMillis)
            .atZone(savedZoneId)
            .withZoneSameInstant(currentZoneId)
            .toInstant()
            .toEpochMilli()

        val selectedDateTime = if (isStartTime) {
            selectedDate.atStartOfDay(currentZoneId).toInstant().toEpochMilli()
        } else {
            selectedDate.plusDays(1).atStartOfDay(currentZoneId).toInstant().toEpochMilli()
        }

        val selectedDateMillis = if (isStartTime) {
            selectedDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        } else {
            selectedDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
        }

        val applyTimeZone = !(isFirstItemInDay || isLastItemInDay)  // Apply time zone to all items except the first and last items, 00:00:00 and 23:59:59

        return if (isStartTime && eventTimeInCurrentZone < selectedDateMillis || !isStartTime && eventTimeInCurrentZone > selectedDateMillis) {   // if the event started before the selected date or ended after the selected date
            val eventDate = java.time.Instant.ofEpochMilli(eventTimeInCurrentZone)
                .atZone(currentZoneId)
                .toLocalDate()
            val daysDifference = if (isStartTime) {
                java.time.temporal.ChronoUnit.DAYS.between(eventDate, selectedDate)
            } else {
                java.time.temporal.ChronoUnit.DAYS.between(selectedDate, eventDate)
            }

            buildString {
                append(
                    if (isStartTime) {
                        selectedDate.minusDays(daysDifference)
                    } else {
                        selectedDate.plusDays(daysDifference)
                    }.dayOfWeek.toString().substring(0, 3)
                )
                append(" ")
                append(timeStringFromLong(eventTimeInCurrentZone, true, currentZoneId, applyTimeZone))
            }
        } else {
            timeStringFromLong(eventTimeInCurrentZone, true, currentZoneId, applyTimeZone)
        }
    }

    /*
    every day must be covered with activities, if there are no activities, fill with a dummy activity that has a start time of 00:00:00 and an end time of 23:59:59
    if there is an activity that starts and end on the same day, then one dummy activity will cover the part of the day before the activity and another dummy activity will cover the part of the day after the activity
     */
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
                steps = null,
                timeZone = java.util.TimeZone.getDefault().id
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
                    steps = null,
                    timeZone = java.util.TimeZone.getDefault().id
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
                        steps = null,
                        timeZone = java.util.TimeZone.getDefault().id
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
                    steps = null,
                    timeZone = java.util.TimeZone.getDefault().id
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
        newSelectedDate: LocalDate,
        filteredActivitiesList: List<ActivitiesList>
    ) {
        selectedDate = newSelectedDate
        val dayActivities: List<Activity> = getDayActivities(
            activities,
            selectedDate,
            filteredActivitiesList
        )
        activitiesOfThisDay = fillWithDummyActivities(dayActivities)

        notifyDataSetChanged()
    }

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }


    private fun timeStringFromLong(
        elapsedTimeMillis: Long,
        showSeconds: Boolean,
        zoneId: ZoneId,
        applyTimeZone: Boolean
    ): String {
        val localDateTime = if (applyTimeZone) {
            java.time.Instant.ofEpochMilli(elapsedTimeMillis).atZone(zoneId).toLocalDateTime()
        } else {
            java.time.Instant.ofEpochMilli(elapsedTimeMillis).atZone(ZoneOffset.UTC).toLocalDateTime()
        }
        val hours = localDateTime.hour.toLong()
        val minutes = localDateTime.minute.toLong()
        val seconds = localDateTime.second.toLong()
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