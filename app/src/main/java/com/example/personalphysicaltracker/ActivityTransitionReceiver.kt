package com.example.personalphysicaltracker

import ActivityRep
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ActivityTransition", "Received intent")
        Log.d("ActivityTransitionReceiver", "$intent")

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent!!)
            if (result != null) {
                for (event in result.transitionEvents) {
                    val activityType = getActivityString(event.activityType)
                    val transitionType = getTransitionString(event.transitionType)
                    val notificationText = "$activityType: $transitionType"

                    context?.let {
                        //send notification
                        sendNotification(it, notificationText)

                        //save in shared preferences
                        val sharedPreferences =
                            it.getSharedPreferences("ActivityTransitionPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                            // Save start time in milliseconds
                            editor.putString("last_activity", activityType)
                            editor.putLong(
                                "start_time",
                                event.elapsedRealTimeNanos / 1000000
                            )
                            editor.apply()
                        } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                            // Save stop time in milliseconds
                            val startTime =
                                sharedPreferences.getLong("start_time", -1)
                            if (startTime != -1L) {
                                val stopTime = event.elapsedRealTimeNanos / 1000000
                                val duration = stopTime - startTime
                                editor.putLong("stop_time", stopTime)
                                editor.putLong("duration", duration)
                                editor.apply()
                            }

                            //register the activity in the database
                            registerActivityInDb(it)
                        }
                    }
                    Log.d("ActivityTransition", notificationText)
                }
            }
        } else {
            Log.d("ActivityTransitionReceiver", "No result in intent")

        }
    }

    private fun registerActivityInDb(context: Context) {
        val sharedPreferences = context.getSharedPreferences("ActivityTransitionPrefs", Context.MODE_PRIVATE)
        val activity = sharedPreferences.getString("last_activity", "")
        val startTime = sharedPreferences.getLong("start_time", -1)
        val stopTime = sharedPreferences.getLong("stop_time", -1)
        val duration = sharedPreferences.getLong("duration", -1)

        val steps: Int? = null
        val timeZone = java.util.TimeZone.getDefault().id

        if (activity != "" && startTime != -1L && stopTime != -1L && duration != -1L) {
            GlobalScope.launch(Dispatchers.IO) {
                val user = ActivityRep.getUser()
                val newActivity = Activity(
                    0,
                    user?.id ?: 0,
                    0,
                    startTime,
                    stopTime,
                    steps,
                    timeZone,
                    true
                )
                ActivityRep.addActivity(newActivity)
            }
        }
    }
    private fun getActivityString(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.STILL -> "Still"
            DetectedActivity.WALKING -> "Walking"
            else -> "Unknown"
        }
    }

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "Enter"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "Exit"
            else -> "Unknown"
        }
    }

    private fun sendNotification(context: Context, text: String) {
        val notificationId = 1
        val channelId = "activity_recognition_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Activity Recognition"
            val descriptionText = "Notifications for activity transitions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Activity Recognition")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId, notificationBuilder.build())
        }
    }
}
