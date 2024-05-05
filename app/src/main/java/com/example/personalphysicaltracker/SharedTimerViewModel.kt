package com.example.personalphysicaltracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedTimerViewModel : ViewModel() {
    private val _elapsedTimeMillis = MutableLiveData<Long>()
    val elapsedTimeMillis: LiveData<Long> get() = _elapsedTimeMillis

    private val _isTimerRunning = MutableLiveData<Boolean>()
    val isTimerRunning: LiveData<Boolean> get() = _isTimerRunning

    fun setElapsedTimeMillis(time: Long) {
        _elapsedTimeMillis.value = time
    }

    fun setIsTimerRunning(isRunning: Boolean) {
        _isTimerRunning.value = isRunning
    }

}
