package com.example.personalphysicaltracker.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.StopWatchService
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var activitiesListViewModel: ActivitiesListViewModel
    //private lateinit var timerJob: Job


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
            //when btn is clicked change src
            if (binding.homeBtnStartAndStop.tag == "start") {  //timer is stopped, we want to start it
                startActivity()
            } else { //timer is running, we want to stop it
                stopActivity()
            }
        }

        //add activity button
        binding.homeBtnNewActivity.setOnClickListener {
            //navigate to add activity fragment
            findNavController().navigate(R.id.action_nav_home_to_addActivity)
        }

        //Stopwatch


        return root
    }



    //@RequiresApi(Build.VERSION_CODES.O)
    private fun startActivity() {
        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_stop_24)
        changeViewTag(binding.homeBtnStartAndStop, "stop")

        startTimer()
        /*Log.d("HomeFragment", "startActivity: $startTime")
        Log.d("HomeFragment", "startActivity: ${parseTime(startTime)}")*/
    }

    private fun stopActivity() {
        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_start_24)
        changeViewTag(binding.homeBtnStartAndStop, "start")

        stopTimer()
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    //return HH:MM:SS String
    private fun parseTime(startTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return startTime.format(formatter)
    }*/

    //@RequiresApi(Build.VERSION_CODES.O)
    //start the timer
    private fun startTimer() {
        /*val startTime = LocalDateTime.now()
        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            var elapsedTime = LocalDateTime.now().minusHours(startTime.hour.toLong())
                .minusMinutes(startTime.minute.toLong())
                .minusSeconds(startTime.second.toLong())
            while (true) {
                binding.homeTimer.text = parseTime(elapsedTime)
                elapsedTime = elapsedTime.plusSeconds(1)
                delay(1000)
            }
        }*/

        val serviceIntent = Intent(requireContext(), StopWatchService::class.java)
        requireContext().startService(serviceIntent)
    }

    private fun stopTimer() {
        /*if (::timerJob.isInitialized && timerJob.isActive) {
            timerJob.cancel()
            Log.d("HomeFragment", "stopTimer: Timer stopped")
        }*/
        val serviceIntent = Intent(requireContext(), StopWatchService::class.java)
        requireContext().stopService(serviceIntent)
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
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("HomeFragment", "onReceive: HO RICEVUTO")//FIXME
            val time = intent?.getLongExtra("time", 0)
            binding.homeTimer.text = formatTime(time ?: 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("STOPWATCH_UPDATED")
        requireContext().registerReceiver(receiver, filter,RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(receiver)
    }

    private fun formatTime(timeInSeconds: Long): String {
        val hours = timeInSeconds / 3600
        val minutes = (timeInSeconds % 3600) / 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}