package com.example.personalphysicaltracker

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

const val CHANNEL_ID = "Stopwatch_channel"

class StopwatchService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StopwatchService {
            return this@StopwatchService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    //timer
    private var isTimerRunning = false
    private var startTime = 0L
    private var stopTime = 0L
    private var elapsedTimeMillis: Long = 0
    private lateinit var timerRunnable: Runnable
    private lateinit var handler: Handler

    //triggered when a component sends an intent to start the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> {
                //start the stopwatch
                startTimer()
            }

            Actions.STOP.name -> {
                //stop the stopwatch
                stopTimer()
            }

            Actions.RESET.name -> {
                //reset the stopwatch
                resetTimer()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        isTimerRunning = true
        startTime = System.currentTimeMillis()

        updateNotification("Elapsed time: ${timeStringFromLong(elapsedTimeMillis)}")
        updateElapsedTime(elapsedTimeMillis)

        handler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    elapsedTimeMillis += 1000
                    updateNotification("Elapsed time: ${timeStringFromLong(elapsedTimeMillis)}")
                    updateElapsedTime(elapsedTimeMillis)
                    handler.postDelayed(this, 1000)
                    //Log.d("StopwatchService", "Elapsed time: ${timeStringFromLong(elapsedTimeMillis)}")
                }
            }
        }
        handler.postDelayed(timerRunnable, 1000)
    }

    private fun timeStringFromLong(elapsedTimeMillis: Long): String {
        val seconds = (elapsedTimeMillis / 1000) % 60
        val minutes = (elapsedTimeMillis / (1000 * 60) % 60)
        val hours = (elapsedTimeMillis / (1000 * 60 * 60) % 24)
        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Long, minutes: Long, seconds: Long): String {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun stopTimer() {
        isTimerRunning = false
        stopTime = System.currentTimeMillis()
        handler.removeCallbacks(timerRunnable)
        updateNotification("Timer stopped: ${timeStringFromLong(elapsedTimeMillis)}")
    }

    private fun resetTimer() {
        isTimerRunning = false
        startTime = 0L
        stopTime = 0L
        elapsedTimeMillis = 0
        updateNotification("Elapsed time: ${timeStringFromLong(elapsedTimeMillis)}")
    }

    private val NOTIFICATION_ID = 1
    private fun updateNotification(text: String) {
        //create an intent to open the MainActivity when the notification is clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        //create the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Physical Activity")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(getColor(R.color.ic_launcher_background))
            .setContentIntent(pendingIntent)
            .build()

        startForeground(
            NOTIFICATION_ID,
            notification
        )
    }

    private val listeners = mutableListOf<StopwatchServiceListener>()

    fun addListener(listener: StopwatchServiceListener) {
        //clear listeners list
        //listeners.removeIf { listener -> true == true }

        listeners.add(listener)
    }

    fun removeListener(listener: StopwatchServiceListener) {
        listeners.remove(listener)
    }

    private fun updateElapsedTime(elapsedTimeMillis: Long) {
        //check if the binding is still active
        listeners.forEach { it.onElapsedTimeChanged(elapsedTimeMillis) }
    }

    enum class Actions {
        START,
        STOP,
        RESET
    }
}