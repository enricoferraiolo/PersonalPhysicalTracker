package com.example.personalphysicaltracker

interface StopwatchServiceListener {
    fun onElapsedTimeChanged(elapsedTimeMillis: Long)
}