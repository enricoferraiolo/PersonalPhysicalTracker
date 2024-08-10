import android.content.Context
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.data.User
import com.example.personalphysicaltracker.data.UserDatabase

object ActivityRep {
    private lateinit var database: UserDatabase

    fun initialize(context: Context) {
        database = UserDatabase.getDatabase(context)
    }

    suspend fun addActivity(activity: Activity) {
        database.activitiesDao().addActivity(activity)
    }

    suspend fun getUser(): User? {
        return database.userDao().readAllData().value?.get(0)
    }
}

