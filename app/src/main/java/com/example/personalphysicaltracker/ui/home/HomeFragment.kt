package com.example.personalphysicaltracker.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.lang.UCharacter.toLowerCase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.compose.ui.text.toLowerCase
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.SharedTimerViewModel
import com.example.personalphysicaltracker.StopwatchControlListener
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
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

    private lateinit var sharedPreferences: SharedPreferences

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var stepCounterStart = 0
    private var isStepCounterAvailable = false


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

        //SharedTimerViewModel
        sharedTimerViewModel =
            ViewModelProvider(requireActivity()).get(SharedTimerViewModel::class.java)

        // Retrieve last selected activity from SharedPreferences
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val savedActivity = sharedPreferences.getString("selectedActivity", null)
        //retrieve steps
        val savedSteps = sharedPreferences.getInt("savedSteps", 0)
        stepCounterStart = sharedPreferences.getInt("stepCounterStart", 0)

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

            //select last saved activity
            if (savedActivity != null) {
                val position = activityNames.indexOf(savedActivity)
                spinner.setSelection(position)
            }
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
                if (resources.getStringArray(R.array.needs_step_counter_activities)
                        .contains(spinner.selectedItem.toString())
                ) {
                    binding.tvSteps.visibility = View.VISIBLE
                } else {
                    binding.tvSteps.visibility = View.GONE
                }

                //save selected activity with SharedPreferences
                val selectedActivity = spinner.selectedItem.toString()
                sharedTimerViewModel.setSelectedActivity(selectedActivity)
                sharedPreferences.edit().putString("selectedActivity", selectedActivity).apply()

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
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //register activity
            val selectedActivity = spinner.selectedItem.toString()
            registerActivity(selectedActivity)
        }

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
                //binding.homeTvWelcome.text = "Welcome, ${users[0].name}!"
                binding.homeTvWelcome.text =
                    "${getString(R.string.home_tv_welcome)}, ${users[0].name}!"
            }
        }


        //step counter and sensor
        sensorManager =
            ContextCompat.getSystemService(requireContext(), SensorManager::class.java)
        //registerStepSensor()

        // load saved steps
        binding.tvSteps.text = "$savedSteps ${getString(R.string.step_emoji)}"
        sharedTimerViewModel.setElapsedSteps(savedSteps)

        return root
    }

    private fun registerStepSensor() {
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor == null) {
            Toast.makeText(requireContext(), "No step counter sensor!", Toast.LENGTH_LONG).show()
        } else {
            isStepCounterAvailable = true
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("HomeFragment", "Step counter sensor available")
        }
    }

    override fun onResume() {
        super.onResume()

        //step sensor
        //registerStepSensor()
    }


    private fun unregisterStepSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()

        //step sensor
        saveStepCount()
        unregisterStepSensor()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isStepCounterAvailable && event != null) {
            if (stepCounterStart == 0) {
                stepCounterStart = event.values[0].toInt()
                sharedPreferences.edit().putInt("stepCounterStart", stepCounterStart).apply()
            }
            val currentSteps = event.values[0].toInt() - stepCounterStart
            sharedTimerViewModel.setElapsedSteps(currentSteps)
            binding.tvSteps.text = "$currentSteps ${getString(R.string.step_emoji)}"
        }
    }

    private fun saveStepCount() {
        val steps = binding.tvSteps.text.toString().split(" ")[0].toInt()
        sharedTimerViewModel.setElapsedSteps(steps)
        sharedPreferences.edit().putInt("savedSteps", steps).apply()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun resetSteps() {
        binding.tvSteps.text = "0 ${getString(R.string.step_emoji)}"
        stepCounterStart = 0
        saveStepCount()
    }

    private fun registerActivity(selectedActivityName: String) {
        //stop timer
        stopTimer(false)
        //step sensor
        saveStepCount()
        unregisterStepSensor()

        //register activity
        activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
            val selectedActivity = activities.find { it.name == selectedActivityName }
            if (selectedActivity != null) {
                //activity found
                //get elapsed time
                val elapsedTime = sharedTimerViewModel.elapsedTimeMillis.value ?: 0
                val startTime = sharedPreferences.getLong("startTime", 0)
                val stopTime = sharedTimerViewModel.stoptime.value ?: 0

                //steps
                var steps: Int? = null
                if (resources.getStringArray(R.array.needs_step_counter_activities)
                        .contains(selectedActivityName)
                ) {
                    steps = binding.tvSteps.text.toString().split(" ")[0].toInt()
                }

                //time zone, get timeZone from the system
                val timeZone = java.util.TimeZone.getDefault().id
                Log.d("HomeFragment", "Time zone: $timeZone")

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
                            steps,
                            timeZone
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
        binding.tvActivityDisplayInfo.visibility = View.VISIBLE
        binding.tvActivityDisplayInfo.text = "${getString(R.string.home_tv_activity_display_info_before_activity_name)} ${toLowerCase(binding.homeSpinner.selectedItem.toString())} ${getString(R.string.home_tv_activity_display_info_after_activity_name)}:"

        //check if the activity needs step counter
        if (resources.getStringArray(R.array.needs_step_counter_activities)
                .contains(binding.homeSpinner.selectedItem.toString())
        ) {
            registerStepSensor()
        }

        changeViewSrc(binding.homeBtnStartAndStop, R.drawable.round_stop_24)
        changeViewTag(binding.homeBtnStartAndStop, "stop")

        //speak with interface to communicate with main activity and stopwatch service
        if (stopwatchAlreadyStarted == false) {
            //start the stopwatch if it has not been started yet
            stopwatchControlListener.startStopwatch()
            //save start time in sharedPreference
            sharedPreferences.edit().putLong("startTime", System.currentTimeMillis()).apply()
        }

        updateElapsedTimeDisplay()
        updateElapsedStepsDisplay()
    }

    private fun resetAction() {
        //timer
        stopTimer(true)
        binding.homeTimer.text = timeStringFromLong(0)

        //activity display info
        binding.tvActivityDisplayInfo.visibility = View.GONE

        //step sensor
        resetSteps()

        stopwatchControlListener.resetStopwatch()
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

    private fun updateElapsedStepsDisplay() {
        sharedTimerViewModel.elapsedSteps.observe(viewLifecycleOwner) { steps ->
            binding.tvSteps.text = "$steps ${getString(R.string.step_emoji)}"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        //step sensor
        //saveStepCount()
        //unregisterStepSensor()
    }


}