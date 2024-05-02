package com.example.personalphysicaltracker.ui.manageActivitylist

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
        Log.d("ManageActivitylistViewModel", "Updating in db: $activity")
        activitiesListViewModel.updateActivity(activity)
    }

    fun deleteActivity(activity: com.example.personalphysicaltracker.data.ActivitiesList) {
        Log.d("ManageActivitylistViewModel", "Deleting from db: $activity")
        activitiesListViewModel.deleteActivity(activity)
    }

}