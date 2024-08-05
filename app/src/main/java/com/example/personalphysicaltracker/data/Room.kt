package com.example.personalphysicaltracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson

//USER TABLE
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int,    //user id
    val name: String    //user name
)

//ACTIVITIES LIST TABLE
@Entity(
    tableName = "activitiesList_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class ActivitiesList(
    @PrimaryKey(autoGenerate = true)
    val id: Int,    //activity id
    var name: String,   //activity name
    val isDefault: Boolean? = false,    //true if the activity is a default activity
    val steps: Int? = null  //number of steps taken during the activity
)

//ACTIVITIES TABLE
@Entity(
    tableName = "activities_table",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActivitiesList::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["activityId"])]
)
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,    //activity id
    val userId: Int,    //user id from user_table
    val activityId: Int?,   //activity id from activitiesList_table
    val startTime: Long,    //time in milliseconds
    val stopTime: Long, //time in milliseconds
    val steps: Int? = null,  //number of steps taken during the activity
    val timeZone: String,   //time zone where the activity was recorded
    val auto: Boolean = false   //true if the activity was automatically detected
)