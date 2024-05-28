package com.example.personalphysicaltracker

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.databinding.CalendarDayLayoutBinding
import com.example.personalphysicaltracker.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class CalendarFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var selectedDate = LocalDate.now()
    private val weekDateFormatter = DateTimeFormatter.ofPattern("dd")
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private lateinit var adapter: CalendarDayAdapter

    private var activitiesList = emptyList<ActivitiesList>()
    private var filterActivities = emptyList<ActivitiesList>()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflating the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu)

        // Find the filter button in the menu
        val filterItem = menu.findItem(R.id.action_filter)

        // Make the filter button visible
        filterItem.isVisible = true

        // Set a click listener for the filter button
        filterItem.setOnMenuItemClickListener {
            // Create and show the AlertDialog
            showFilterDialog()

            true // Return true to consume the click event
        }
    }

    private fun showFilterDialog() {
        // Inflate the dialog layout
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_filter_activities, null)

        // Initialize RecyclerView and its adapter
        val recyclerView =
            dialogView.findViewById<RecyclerView>(R.id.dialog_filter_recycler_view_activities)
        val adapterDialog = DialogActivitiesAdapter(activitiesList, filterActivities)
        recyclerView.adapter = adapterDialog
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Build and show the AlertDialog
        AlertDialog.Builder(requireContext())
            .setTitle("Select Activities to show")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val selectedActivities = adapterDialog.getSelectedActivities()
                filterActivities = selectedActivities
                binding.weekCalendarView.notifyCalendarChanged()
                adapter.updateData(selectedDate, filterActivities)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeDefaultDate()

        //view model
        val activitiesViewModel = ViewModelProvider(this).get(ActivitiesViewModel::class.java)
        val activitiesListViewModel =
            ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = CalendarDayLayoutBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.weekCalendarView.notifyDateChanged(day.date)
                        oldDate?.let { binding.weekCalendarView.notifyDateChanged(it) }

                        //binding.tvSelectedDate.text = fullDateFormatter.format(day.date).toString()
                        adapter.updateData(day.date, filterActivities)
                        //load the activities for the day
                        loadDayActivities(day.date)
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                bind.calendarDateText.text = weekDateFormatter.format(day.date)
                bind.calendarDayText.text = day.date.dayOfWeek.displayText()

                val colorRes = if (day.date == selectedDate) {
                    R.color.purple_500
                } else {
                    R.color.black
                }
                bind.calendarDateText.setTextColor(view.context.getColorCompat(colorRes))
                bind.calendarDayText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenSelectedView.isVisible = day.date == selectedDate

                //check if there are activities for the day
                activitiesViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
                    val activitiesOfDay =
                        getDayActivities(activities, day.date, filterActivities)
                    if (activitiesOfDay.isNotEmpty()) {
                        bind.flCalendarDayLayout.setBackgroundColor(view.context.getColorCompat(R.color.green_500))
                    } else {
                        bind.flCalendarDayLayout.setBackgroundColor(view.context.getColorCompat(R.color.gray_500))
                    }
                }
            }


        }

        binding.weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        binding.weekCalendarView.weekScrollListener = { weekDays ->
            //change fragment label
            (activity as? AppCompatActivity)?.supportActionBar?.title = getWeekPageTitle(weekDays)
        }

        // Setup the calendar view
        setupCalendar(activitiesViewModel, activitiesListViewModel)

        //recyclerview
        // val adapter = CalendarDayAdapter()
        val recyclerView = binding.rvDayActivities
        // recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        //load the activities from the database in the recyclerview
        activitiesViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
            activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activitiesList ->
                //check if activities is not empty
                if (activities.isNotEmpty()) {
                    //check if activitiesList is not empty
                    if (activitiesList.isNotEmpty()) {
                        adapter = CalendarDayAdapter(
                            activities,
                            activitiesList,
                            selectedDate
                        )
                        recyclerView.adapter = adapter
                        binding.tvNoActivities.isVisible = false

                        this.activitiesList = activitiesList
                        this.filterActivities = activitiesList
                    }
                } else {
                    binding.tvNoActivities.isVisible = true
                }
            }
        }


        return root
    }

    private fun initializeDefaultDate() {
        //binding.tvSelectedDate.text = fullDateFormatter.format(selectedDate)
        loadDayActivities(selectedDate)
    }

    private fun loadDayActivities(date: LocalDate) {
        //TODO("Not yet implemented")
    }

    private fun setupCalendar(
        activitiesViewModel: ActivitiesViewModel,
        activitiesListViewModel: ActivitiesListViewModel
    ) {
        val currentMonth = YearMonth.now()

        var monthsToSubstract: Long = 0
        var monthsToAdd: Long = 0


        //we need to get the first ever activity record registered in the database
        //and get the month and year of that record
        //then we will use that month and year to set the start of the calendar
        val combinedLiveData = MediatorLiveData<Pair<Activity?, Activity?>>().apply {
            var firstActivity: Activity? = null
            var latestActivity: Activity? = null

            addSource(activitiesViewModel.getFirstActivity()) { activity ->
                firstActivity = activity
                value = firstActivity to latestActivity
            }

            addSource(activitiesViewModel.getLatestActivity()) { activity ->
                latestActivity = activity
                value = firstActivity to latestActivity
            }
        }

        combinedLiveData.observe(viewLifecycleOwner, Observer { (firstActivity, latestActivity) ->
            if (firstActivity != null && latestActivity != null) {
                val startMonth = YearMonth.of(
                    getYear(firstActivity.startTime).toInt(),
                    getMonth(firstActivity.startTime).toInt()
                )
                val endMonth = YearMonth.of(
                    getYear(latestActivity.stopTime).toInt(),
                    getMonth(latestActivity.stopTime).toInt()
                )

                val monthsToSubtract =
                    ChronoUnit.MONTHS.between(startMonth, currentMonth).coerceAtLeast(0)
                val monthsToAdd = ChronoUnit.MONTHS.between(currentMonth, endMonth).coerceAtLeast(0)


                binding.weekCalendarView.setup(
                    currentMonth.minusMonths(monthsToSubtract)
                        .atDay(getDayNumber(firstActivity.startTime)), //bound to first day of the week
                    currentMonth.plusMonths(monthsToAdd)
                        .atDay(getDayNumber(latestActivity.stopTime)), //bound to last day of the week
                    firstDayOfWeekFromLocale()
                )
                binding.weekCalendarView.scrollToDate(LocalDate.now())


                activitiesViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
                    activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activitiesList ->
                        //check if activitiesList is not empty
                        if (activitiesList.isNotEmpty()) {
                            this.activitiesList = activitiesList
                            this.filterActivities = activitiesList
                            binding.weekCalendarView.notifyCalendarChanged()

                        }
                    }
                }
            }

        })

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun timeStringFromLong(elapsedTimeMillis: Long, showSeconds: Boolean): String {
        val seconds = (elapsedTimeMillis / 1000) % 60
        val minutes = (elapsedTimeMillis / (1000 * 60) % 60)
        val hours = (elapsedTimeMillis / (1000 * 60 * 60) % 24)
        return makeTimeString(hours, minutes, seconds, showSeconds)
    }

    private fun makeTimeString(
        hours: Long,
        minutes: Long,
        seconds: Long,
        showSeconds: Boolean
    ): String {
        return if (showSeconds) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", hours, minutes)
        }
    }
}