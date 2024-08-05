package com.example.personalphysicaltracker

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
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

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
                        sendNotification(it, notificationText)
                    }
                    Log.d("ActivityTransition", notificationText)
                }
            }
        }else{
            Log.d("ActivityTransitionReceiver", "No result in intent")

        }
    }

    /*override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ActivityUpdateReceiver", "onReceive called")
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = intent?.let { ActivityRecognitionResult.extractResult(it) }
            result?.let {
                val mostProbableActivity = it.mostProbableActivity
                val activityType = getActivityString(mostProbableActivity.type)
                val notificationText = "Current activity: $activityType"

                context?.let {
                    sendNotification(it, notificationText)
                }
                Log.d("ActivityUpdateReceiver", notificationText)
            }
        } else {
            Log.d("ActivityUpdateReceiver", "No ActivityRecognitionResult in intent")
        }
    }*/

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, notificationBuilder.build())
        }
    }
}
