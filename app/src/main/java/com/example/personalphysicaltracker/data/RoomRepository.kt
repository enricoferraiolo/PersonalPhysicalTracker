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
}

//ACTIVITIES REPOSITORY
class ActivitiesRepository(private val activitiesDao: ActivitiesDao) {
    val readAllData: LiveData<List<Activity>> = activitiesDao.readAllData()

    suspend fun addActivity(activity: Activity) {
        activitiesDao.addActivity(activity)
    }
}
