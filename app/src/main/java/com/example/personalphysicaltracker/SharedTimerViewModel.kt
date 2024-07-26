package com.example.personalphysicaltracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedTimerViewModel : ViewModel() {
    //timer
    private val _elapsedTimeMillis = MutableLiveData<Long>()
    val elapsedTimeMillis: LiveData<Long> get() = _elapsedTimeMillis

    private val _isTimerRunning = MutableLiveData<Boolean>()
    val isTimerRunning: LiveData<Boolean> get() = _isTimerRunning

    private val _startTime = MutableLiveData<Long>()
    val startTime: LiveData<Long> get() = _startTime

    private val _stoptime = MutableLiveData<Long>()
    val stoptime: LiveData<Long> get() = _stoptime

    fun setElapsedTimeMillis(time: Long) {
        _elapsedTimeMillis.value = time
    }

    fun setIsTimerRunning(isRunning: Boolean) {
        _isTimerRunning.value = isRunning
    }

    fun setStartTime(time: Long) {
        _startTime.value = time
    }

    fun setStopTime(time: Long) {
        _stoptime.value = time
    }

    //step counter
    private val _elapsedSteps = MutableLiveData<Int>()
    val elapsedSteps: LiveData<Int> get() = _elapsedSteps

    fun setElapsedSteps(steps: Int) {
        _elapsedSteps.value = steps
    }

    //selected activity
    private val _selectedActivity = MutableLiveData<String>()
    val selectedActivity: LiveData<String> get() = _selectedActivity

    fun setSelectedActivity(activity: String) {
        _selectedActivity.value = activity
    }
}
