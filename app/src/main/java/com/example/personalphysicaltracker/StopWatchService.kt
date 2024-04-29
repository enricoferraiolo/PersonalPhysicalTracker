package com.example.personalphysicaltracker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopWatchService : Service() {
    private var timerJob: Job? = null
    private var elapsedTime = 0L
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //start the stopwatch
        startTimer()
        return START_STICKY
    }

    override fun onDestroy() {
        stopTimer()
        super.onDestroy()
    }

    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                Log.d("StopWatchService", "startTimer: $elapsedTime")
                delay(1000)
                elapsedTime++
                // Invia un broadcast per aggiornare l'UI con il tempo trascorso
                val intent = Intent("STOPWATCH_UPDATED")
                intent.putExtra("time", elapsedTime)
                sendBroadcast(intent)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }
}