package com.example.personalphysicaltracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.databinding.CalendarDayLayoutBinding
import com.example.personalphysicaltracker.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class CalendarFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var selectedDate = LocalDate.now()
    private val weekDateFormatter = DateTimeFormatter.ofPattern("dd")
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private lateinit var adapter: CalendarDayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeDefaultDate()

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
                        adapter.updateData(day.date)
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
                    R.color.purple_200
                } else {
                    R.color.black
                }
                bind.calendarDateText.setTextColor(view.context.getColorCompat(colorRes))
                bind.calendarDayText.setTextColor(view.context.getColorCompat(colorRes))
                bind.exSevenSelectedView.isVisible = day.date == selectedDate
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
        setupCalendar()

        //recyclerview
        // val adapter = CalendarDayAdapter()
        val recyclerView = binding.rvDayActivities
        // recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //view model
        val activitiesViewModel = ViewModelProvider(this).get(ActivitiesViewModel::class.java)
        val activitiesListViewModel =
            ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

        activitiesViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
            activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activitiesList ->
                adapter = CalendarDayAdapter(activities, activitiesList, selectedDate)
                recyclerView.adapter = adapter
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

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()

        val monthsToSubstract = 0

        //we need to get the first ever activity record registered in the database
        //and get the month and year of that record
        //then we will use that month and year to set the start of the calendar


        binding.weekCalendarView.setup(
            currentMonth.minusMonths(5).atStartOfMonth(),
            currentMonth.plusMonths(0).atEndOfMonth(),
            firstDayOfWeekFromLocale(),
        )
        binding.weekCalendarView.scrollToDate(LocalDate.now())
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