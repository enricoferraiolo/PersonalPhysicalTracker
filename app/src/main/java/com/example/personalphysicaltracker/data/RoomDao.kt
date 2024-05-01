package com.example.personalphysicaltracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

//USER DAO
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Query("SELECT * FROM user_table")
    fun readAllData(): LiveData<List<User>>

    @Query("DELETE FROM user_table")
    suspend fun resetDB()

    @Query("SELECT COUNT(*) FROM user_table")
    fun userCount(): LiveData<Int>
}

//ACTIVITIES LIST DAO
@Dao
interface ActivitiesListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addActivity(activity: ActivitiesList)

    @Query("SELECT * FROM activitiesList_table")
    fun readAllData(): LiveData<List<ActivitiesList>>

    @Delete
    suspend fun deleteActivity(activity: ActivitiesList)

    @Update
    suspend fun updateActivity(activity: ActivitiesList)
}

//ACTIVITIES DAO
@Dao
interface ActivitiesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addActivity(activity: Activity)

    @Query("SELECT * FROM activities_table")
    fun readAllData(): LiveData<List<Activity>>
}