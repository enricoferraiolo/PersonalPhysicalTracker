package com.example.personalphysicaltracker.ui.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.SharedTimerViewModel
import com.example.personalphysicaltracker.StopwatchControlListener
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.data.ExtraInfo
import com.example.personalphysicaltracker.data.UserViewModel
import com.example.personalphysicaltracker.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Timer


class HomeFragment : Fragment(), SensorEventListener {
    private lateinit var userViewModel: UserViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var activitiesListViewModel: ActivitiesListViewModel //activities list view model
    private lateinit var activitiesViewModel: ActivitiesViewModel //activities registered view model
    private lateinit var stopwatchControlListener: StopwatchControlListener
    private lateinit var sharedTimerViewModel: SharedTimerViewModel

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var totalSteps = 0
    private var previousTotalSteps = 0
    private val needsStepCounterActivities: List<String> = listOf("Walking", "Running")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is StopwatchControlListener) {
            stopwatchControlListener = context
        } else {
            throw RuntimeException("$context must implement StopwatchControlListener")
        }
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val timer = Timer()

    private var isTimerRunning = false

    // spinner is NOT selected when the fragment is created
    private var isFirstSpinnerSelection = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Set OnItemSelectedListener for the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Call resetAction() when a new item is selected
                //oncreateview is called and it selects an item when the fragment is created, so we need to check if it is the first selection
                if (!isFirstSpinnerSelection) {
                    resetAction()
                } else {
                    //no reset if it is the first selection
                    isFirstSpinnerSelection = false
                }

                //check if activity need step counter
                if (needsStepCounterActivities.contains(spinner.selectedItem.toString())) {
                    binding.tvSteps.visibility = View.VISIBLE
                } else {
                    binding.tvSteps.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }

        //activities view model
        activitiesViewModel = ViewModelProvider(this).get(ActivitiesViewModel::class.java)

        //user view model
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        //btn start activity onclicklister
        binding.homeBtnStartAndStop.setOnClickListener {
            Log.d("HomeFragment", "-- btn start/stop clicked --")
            startStopAction()
        }

        //btn reset onclicklistener
        binding.homeBtnReset.setOnClickListener {
            //reset timer
            resetAction()
        }

        //btn register activity onclicklistener
        binding.homeBtnRegisterActivity.setOnClickListener {
            //check if timer is running
            if (binding.homeTimer.text == "00:00:00") {
                Toast.makeText(
                    requireContext(),
                    "Start the timer before registering an activity!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            //register activity
            val selectedActivity = spinner.selectedItem.toString()
            registerActivity(selectedActivity)
        }

        //SharedTimerViewModel
        sharedTimerViewModel =
            ViewModelProvider(requireActivity()).get(SharedTimerViewModel::class.java)

        //check if timer is running from sharedTimerViewModel
        sharedTimerViewModel.elapsedTimeMillis.observe(viewLifecycleOwner) { elapsedTime ->
            binding.homeTimer.text = timeStringFromLong(elapsedTime)
            if (elapsedTime > 0) {
                startTimer(true)
            }
        }

        //welcome message
        //get user name from db
        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.readAllData.observe(viewLifecycleOwner) { users ->
            if (users.isNotEmpty()) {
                binding.homeTvWelcome.text = "Welcome, ${users[0].name}!"
            }
        }


        //step counter and sensor
        sensorManager =
            ContextCompat.getSystemService(requireContext(), SensorManager::class.java)
        //registerStepSensor()


        return root
    }

    private fun registerStepSensor() {
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor == null) {
            Toast.makeText(requireContext(), "No step counter sensor!", Toast.LENGTH_LONG).show()
        } else {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    private fun unregisterStepSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()

        //step sensor
        //sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        totalSteps = event!!.values[0].toInt()
        val currentSteps = totalSteps - previousTotalSteps
        binding.tvSteps.text = "Steps: $currentSteps"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun resetSteps() {
        previousTotalSteps = totalSteps
        binding.tvSteps.text = "Steps: 0"
        saveData()
    }

    private fun saveData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getInt("key1", 0)
        Log.d("HomeFragment", "savedNumber: $savedNumber")
        previousTotalSteps = savedNumber
    }


    private fun registerActivity(selectedActivityName: String) {
        //stop timer
        stopTimer(false)

        //register activity
        activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
            val selectedActivity = activities.find { it.name == selectedActivityName }
            if (selectedActivity != null) {
                //activity found
                //get elapsed time
                val elapsedTime = sharedTimerViewModel.elapsedTimeMillis.value ?: 0
                val startTime = sharedTimerViewModel.startTime.value ?: 0
                val stopTime = sharedTimerViewModel.stoptime.value ?: 0

                //check if time elapsed is greater than 0, if so, register activity
                if (elapsedTime > 0) {
                    //register activity
                    activitiesViewModel.addActivity(
                        Activity(
                            0,
                            userViewModel.readAllData.value?.get(0)?.id ?: 0,
                            selectedActivity.id,
                            startTime,
                            stopTime,
                            ExtraInfo(
                                selectedActivity.extra?.stepsSelector ?: false,
                                selectedActivity.extra?.metersSelector ?: false,
                                selectedActivity.extra?.steps ?: 0,
                                selectedActivity.extra?.meters ?: 0
                            )
                        )
                    )

                    //reset timer
                    resetAction()

                    //toast
                    Toast.makeText(requireContext(), "Activity registered!", Toast.LENGTH_LONG)
                        .show()

                } else {
                    Log.d("HomeFragment", "No time elapsed ")
                }
            } else {
                Log.d("HomeFragment", "No activity selected ")
            }
        }
    }


    private fun startStopAction() {
        //observe the timer state
        if (binding.homeBtnStartAndStop.tag == "start") {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun stopTimer(stopwatchAlreadyStopped: Boolean = false) {
        Log.d("HomeFragment", "stopTimer called")

        //step sensor
        unregisterStepSensor()

        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_start_24)
        changeViewTag(binding.homeBtnStartAndStop, "start")

        //speak with interface to communicate with main activity and stopwatch service
        if (stopwatchAlreadyStopped == false) {
            //be careful when putting stopwatchAlreadyStopped to true, it is also used by resetAction()
            //stop the stopwatch if it has not been started yet
            stopwatchControlListener.stopStopwatch()
        }
    }

    private fun startTimer(stopwatchAlreadyStarted: Boolean = false) {
        Log.d(
            "HomeFragment",
            "startTimer called, stopwatchAlreadyStarted: $stopwatchAlreadyStarted"
        )

        //check if the activity needs step counter
        if (needsStepCounterActivities.contains(binding.homeSpinner.selectedItem.toString())) {
            registerStepSensor()
        }

        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_stop_24)
        changeViewTag(binding.homeBtnStartAndStop, "stop")

        //speak with interface to communicate with main activity and stopwatch service
        if (stopwatchAlreadyStarted == false) {
            //start the stopwatch if it has not been started yet
            stopwatchControlListener.startStopwatch()
        }

        updateElapsedTimeDisplay()
    }

    private fun resetAction() {
        stopTimer(true)
        binding.homeTimer.text = timeStringFromLong(0)
        stopwatchControlListener.resetStopwatch()

        resetSteps()

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

    private fun updateElapsedTimeDisplay() {
        sharedTimerViewModel.elapsedTimeMillis.observe(viewLifecycleOwner) { elapsedTime ->
            binding.homeTimer.text = timeStringFromLong(elapsedTime)
        }
    }

    private fun updateElapsedTimeDisplay(n: Long) {
        binding.homeTimer.text = timeStringFromLong(n)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


}