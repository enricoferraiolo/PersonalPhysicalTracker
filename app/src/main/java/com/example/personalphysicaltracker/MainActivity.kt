package com.example.personalphysicaltracker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.UserViewModel
import com.example.personalphysicaltracker.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import java.util.Timer
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), StopwatchServiceListener, StopwatchControlListener {
    override fun onElapsedTimeChanged(elapsedTimeMillis: Long) {
        Log.d("MainActivity - sharedTimerViewModel", "Elapsed time: $elapsedTimeMillis")
        sharedTimerViewModel.setElapsedTimeMillis(elapsedTimeMillis)
    }

    private lateinit var userViewModel: UserViewModel
    private lateinit var activitiesListViewModel: ActivitiesListViewModel
    private lateinit var sharedTimerViewModel: SharedTimerViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    private val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ),
                0
            )
        }


        //check if database is empty
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)


        //init db

        //check if user is registered, otherwise redirect to registration activity
        userViewModel.userCount().observe(this) { userCount ->
            if (userCount == 0) {
                // no user is in db, redirect to registration activity
                val intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
                finish() // user will not be able to go back to MainActivity
            }
        }

        //check if array of activities is empty, if so, fill it with default activities
        activitiesListViewModel = ViewModelProvider(this).get(ActivitiesListViewModel::class.java)
        activitiesListViewModel.readAllData.observe(this) { activities ->
            if (activities.isEmpty()) {
                // no activities in db, fill it with default activities
                val defaultActivities = resources.getStringArray(R.array.default_activityList)
                for (activityName in defaultActivities) {
                    val newActivity =
                        ActivitiesList(
                            0,
                            activityName,
                            true,
                            null
                        )
                    activitiesListViewModel.addActivity(newActivity)
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_charts,
                R.id.nav_info,
                R.id.nav_manageActivityList,
                R.id.calendarFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //SharedTimerViewModel
        sharedTimerViewModel = ViewModelProvider(this).get(SharedTimerViewModel::class.java)

        // Trigger notification
        schedulePeriodicNotification(6, TimeUnit.HOURS)
    }

    override fun onResume() {
        super.onResume()

        // Bind to stopwatch service
        bindToStopwatchService()
    }

    private fun schedulePeriodicNotification(interval: Long, timeUnit: TimeUnit) {
        val notificationWork = PeriodicWorkRequest.Builder(
            NotificationWorker::class.java,
            interval, timeUnit
        )
            .setInitialDelay(
                interval,
                timeUnit
            )//set initial delay before sending the first notification
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notificationWork",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            notificationWork
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StopwatchService.LocalBinder
            val stopwatchService = binder.getService()


            stopwatchService.addListener(this@MainActivity)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle service disconnection
        }
    }

    private fun bindToStopwatchService() {
        val intent = Intent(applicationContext, StopwatchService::class.java)
        applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        Log.d("MainActivity", "Service bound")
    }

    private fun unbindFromStopwatchService() {
        applicationContext.unbindService(serviceConnection)

        serviceConnection.onServiceDisconnected(null)


        Log.d("MainActivity", "Service unbound")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        //hide filter btn
        menu.findItem(R.id.action_filter).isVisible = false

        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onDestroy() {
        super.onDestroy()

        // Unbind from service
        unbindFromStopwatchService()
    }

    override fun startStopwatch() {
        Log.d("MainActivity", "isTimerRunning: ${sharedTimerViewModel.isTimerRunning.value}")
        sharedTimerViewModel.setIsTimerRunning(true)
        sharedTimerViewModel.setStartTime(System.currentTimeMillis())

        // Avvia il cronometro nel servizio StopwatchService
        val intent = Intent(this, StopwatchService::class.java)
        intent.action = StopwatchService.Actions.START.name
        startService(intent)
    }

    override fun stopStopwatch() {
        Log.d("MainActivity", "isTimerRunning: STOPPED")
        sharedTimerViewModel.setIsTimerRunning(false)
        sharedTimerViewModel.setStopTime(System.currentTimeMillis())

        // Ferma il cronometro nel servizio StopwatchService
        val intent = Intent(this, StopwatchService::class.java)
        intent.action = StopwatchService.Actions.STOP.name
        startService(intent)
    }

    override fun resetStopwatch() {
        sharedTimerViewModel.setIsTimerRunning(false)
        sharedTimerViewModel.setElapsedTimeMillis(0)
        sharedTimerViewModel.setStartTime(0)
        sharedTimerViewModel.setStopTime(0)

        // Resetta il cronometro nel servizio StopwatchService
        val intent = Intent(this, StopwatchService::class.java)
        intent.action = StopwatchService.Actions.RESET.name
        startService(intent)
    }

}