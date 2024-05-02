package com.example.personalphysicaltracker.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.StopwatchService
import com.example.personalphysicaltracker.StopwatchServiceListener
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.UserViewModel
import com.example.personalphysicaltracker.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Timer


class HomeFragment : Fragment(), StopwatchServiceListener {
    private lateinit var userViewModel: UserViewModel

    private var _binding: FragmentHomeBinding? = null

    private lateinit var activitiesListViewModel: ActivitiesListViewModel


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val timer = Timer()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //activities spinner
        //fill spinner with activities from db
        val spinner: Spinner = binding.homeSpinner
        activitiesListViewModel = ViewModelProvider(this).get(ActivitiesListViewModel::class.java)
        activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
            val activityNames = activities.map { it.name }
            //fill spinner with activities
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }


        //btn start activity onclicklister
        binding.homeBtnStartAndStop.setOnClickListener {
            startStopAction()
        }

        //btn reset onclicklistener
        binding.homeBtnReset.setOnClickListener {
            //reset timer
            resetAction()
        }


        //Stopwatch


        //welcome message
        //get user name from db
        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty()) {
                binding.homeTvWelcome.text = "Welcome, ${users[0].name}!"
            }
        }

        return root
    }

    private var isTimerRunning = false

    private fun startStopAction() {
        isTimerRunning = !isTimerRunning // Toggle lo stato del timer

        if (isTimerRunning) {
            startTimer()
        } else {
            stopTimer()
        }


    }

    private fun stopTimer() {
        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_start_24)
        changeViewTag(binding.homeBtnStartAndStop, "start")

        //send intent to StopwatchService to stop the service
        Intent(StopwatchService.Actions.STOP.name).also {
            it.setClass(requireContext(), StopwatchService::class.java)
            requireContext().startService(it)
        }
    }

    private fun startTimer() {
        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_stop_24)
        changeViewTag(binding.homeBtnStartAndStop, "stop")

        //send intent to StopwatchService to start the service
        Intent(StopwatchService.Actions.START.name).also {
            it.setClass(requireContext(), StopwatchService::class.java)
            requireContext().startService(it)
        }

    }

    private fun resetAction() {
        stopTimer()
        binding.homeTimer.text = timeStringFromLong(0)
    }


    private fun timeStringFromLong(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60) % 60)
        val hours = (ms / (1000 * 60 * 60) % 24)
        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hours: Long, minutes: Long, seconds: Long): String {
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    private fun changeViewSrc(button: FloatingActionButton, drawable: Int) {
        button.setImageResource(drawable)
    }

    private fun changeViewTag(button: FloatingActionButton, tag: String) {
        button.tag = tag
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Unbind from service
        requireContext().unbindService(serviceConnection)
    }

    override fun onResume() {
        super.onResume()

        // Bind to service
        bindToService()
    }

    // StopwatchServiceListener
    override fun onElapsedTimeChanged(elapsedTimeMillis: Long) {
        // Update UI with elapsed time
        binding.homeTimer.text = timeStringFromLong(elapsedTimeMillis)
        Log.d("HomeFragment", "Elapsed time: $elapsedTimeMillis")
    }

    private fun bindToService() {
        val intent = Intent(requireContext(), StopwatchService::class.java)
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d("HomeFragment", "Service bound")
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StopwatchService.LocalBinder
            val stopwatchService = binder.getService()
            stopwatchService.addListener(this@HomeFragment)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle service disconnection
        }
    }

}