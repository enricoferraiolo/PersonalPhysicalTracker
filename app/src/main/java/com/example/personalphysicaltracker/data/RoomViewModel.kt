package com.example.personalphysicaltracker.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//USER VIEW MODEL
class UserViewModel(application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<User>>
    private val repository: UserRepository

    init {
        val userDao = UserDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllData = repository.readAllData
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun resetDB() {
        //drop all tables and recreate them
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetDB()
        }
    }

    fun userCount(): LiveData<Int> {
        return repository.userCount()
    }
}

//ACTIVITIES LIST VIEW MODEL
class ActivitiesListViewModel(application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<ActivitiesList>>
    private val repository: ActivitiesListRepository

    init {
        val activitiesListDao = UserDatabase.getDatabase(application).activitiesListDao()
        repository = ActivitiesListRepository(activitiesListDao)
        readAllData = repository.readAllData
    }

    fun addActivity(activity: ActivitiesList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addActivity(activity)
        }
    }

    fun deleteActivity(activity: ActivitiesList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteActivity(activity)
        }
    }

    fun updateActivity(activity: ActivitiesList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateActivity(activity)
        }
    }
}

//ACTIVITIES VIEW MODEL
class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {
    val readAllData: LiveData<List<Activity>>
    private val repository: ActivitiesRepository

    init {
        val activitiesDao = UserDatabase.getDatabase(application).activitiesDao()
        repository = ActivitiesRepository(activitiesDao)
        readAllData = repository.readAllData
    }

    fun addActivity(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addActivity(activity)
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteActivity(activity)
        }
    }

    fun getFirstActivity(): LiveData<Activity?> {
        return repository.getFirstActivity()
    }

    fun getLatestActivity(): LiveData<Activity?> {
        return repository.getLatestActivity()
    }

}