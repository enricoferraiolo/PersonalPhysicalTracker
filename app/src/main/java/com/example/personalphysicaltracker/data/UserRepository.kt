package com.example.personalphysicaltracker.data

import androidx.lifecycle.LiveData

class UserRepository(private val userdao: UserDao) {

    val readAllData: LiveData<List<User>> = userdao.readAllData()

    suspend fun addUser(user: User) {
        userdao.addUser(user)
    }
}