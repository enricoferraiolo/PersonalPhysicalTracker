package com.example.personalphysicaltracker.data

import androidx.lifecycle.LiveData

//USER REPOSITORY
class UserRepository(private val userdao: UserDao) {
    val readAllData: LiveData<List<User>> = userdao.readAllData()

    suspend fun addUser(user: User) {
        userdao.addUser(user)
    }

    suspend fun resetDB() {
        userdao.resetDB()
    }

    fun userCount(): LiveData<Int> {
        return userdao.userCount()
    }
}

//ACTIVITIES LIST REPOSITORY
class ActivitiesListRepository(private val activitiesListDao: ActivitiesListDao) {
    val readAllData: LiveData<List<ActivitiesList>> = activitiesListDao.readAllData()

    suspend fun addActivity(activity: ActivitiesList) {
        activitiesListDao.addActivity(activity)
    }

    suspend fun deleteActivity(activity: ActivitiesList) {
        activitiesListDao.deleteActivity(activity)
    }

    suspend fun updateActivity(activity: ActivitiesList) {
        activitiesListDao.updateActivity(activity)
    }
}

//ACTIVITIES REPOSITORY
class ActivitiesRepository(private val activitiesDao: ActivitiesDao) {
    val readAllData: LiveData<List<Activity>> = activitiesDao.readAllData()

    suspend fun addActivity(activity: Activity) {
        activitiesDao.addActivity(activity)
    }

    fun getFirstActivity(): LiveData<Activity?> {
        return activitiesDao.getFirstActivity()
    }

    fun getLatestActivity(): LiveData<Activity?> {
        return activitiesDao.getLatestActivity()
    }

}
