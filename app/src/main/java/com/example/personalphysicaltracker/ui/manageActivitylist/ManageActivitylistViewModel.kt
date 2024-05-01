package com.example.personalphysicaltracker.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.data.ActivitiesListViewModel

class ManageActivitylistViewModel<ActivitiesList> : ViewModel() {
    private val activitiesListViewModel: ActivitiesListViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(Application()).create(ActivitiesListViewModel::class.java)
    }
    fun editActivity(activity: com.example.personalphysicaltracker.data.ActivitiesList) {
        Log.d("ManageActivitylistViewModel", "editing: $activity")
        TODO("Not yet implemented")
        activitiesListViewModel.updateActivity(activity)
    }

    fun deleteActivity(activity: com.example.personalphysicaltracker.data.ActivitiesList) {
        Log.d("ManageActivitylistViewModel", "deleting: $activity")
        activitiesListViewModel.deleteActivity(activity)

    }

}