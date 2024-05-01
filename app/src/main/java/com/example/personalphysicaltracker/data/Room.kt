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
    val id: Int,
    val name: String
)

data class ExtraInfo(
    val stepsSelector: Boolean = false,
    val metersSelector: Boolean = false,
    val steps: Int? = null,
    val meters: Int? = null,
)

class ExtraInfoConverter {
    @TypeConverter
    fun fromExtraInfo(extraInfo: ExtraInfo?): String? {
        return extraInfo?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toExtraInfo(json: String?): ExtraInfo? {
        return json?.let { Gson().fromJson(it, ExtraInfo::class.java) }
    }
}

//ACTIVITIES LIST TABLE
@Entity(
    tableName = "activitiesList_table",
    indices = [Index(value = ["name"], unique = true)]
)
data class ActivitiesList(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val isDefault: Boolean? = false,
    val extra: ExtraInfo?
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
            parentColumns = ["name"],
            childColumns = ["activityName"],
            onDelete = ForeignKey.NO_ACTION //if activity is deleted, keep the activity in the db
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["activityName"])]
)
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val userId: Int,
    val activityName: Int,
    val startTime: Long,
    val finishTime: Long,
    val extra: ExtraInfo?
)