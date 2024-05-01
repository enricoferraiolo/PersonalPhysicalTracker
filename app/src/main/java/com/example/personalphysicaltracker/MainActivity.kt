package com.example.personalphysicaltracker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ExtraInfo
import com.example.personalphysicaltracker.data.UserViewModel
import com.example.personalphysicaltracker.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var activitiesListViewModel: ActivitiesListViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    val newActivity = ActivitiesList(activityName,true, ExtraInfo(false, false, null, null))
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
                R.id.nav_home, R.id.nav_charts, R.id.nav_info
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}