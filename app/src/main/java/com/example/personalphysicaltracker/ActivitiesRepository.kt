package com.example.personalphysicaltracker

import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.UserViewModel

object ActivitiesRepository {

    private var activitiesViewModel: ActivitiesViewModel? = null
    private var userViewModel: UserViewModel? = null

    fun initialize(viewModel: ActivitiesViewModel, userViewModel: UserViewModel) {
        activitiesViewModel = viewModel
        this.userViewModel = userViewModel
    }

    fun getActivitiesViewModel(): ActivitiesViewModel? {
        return activitiesViewModel
    }

    fun getUserViewModel(): UserViewModel? {
        return userViewModel
    }
}