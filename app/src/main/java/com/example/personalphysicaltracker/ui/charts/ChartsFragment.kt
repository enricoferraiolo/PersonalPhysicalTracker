package com.example.personalphysicaltracker.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.databinding.FragmentChartsBinding
import com.example.personalphysicaltracker.displayText
import com.example.personalphysicaltracker.getColorCompat
import com.example.personalphysicaltracker.getMonth
import com.example.personalphysicaltracker.getYear
import com.example.personalphysicaltracker.isDarkModeEnabled
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.DateValidatorPointForward
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null

    private val binding get() = _binding!!

    private var activitiesList = emptyList<ActivitiesList>()
    private var activities = emptyList<Activity>()

    private var currentYearMonth: YearMonth = YearMonth.now()
    private lateinit var firstYearMonth: YearMonth
    private lateinit var lastYearMonth: YearMonth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val chartsViewModel =
            ViewModelProvider(this).get(ChartsViewModel::class.java)

        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //check if system has dark mode enabled
        if (isDarkModeEnabled(requireContext())) {
            binding.tvChartsNoActivities.setTextColor(
                requireContext().getColorCompat(
                    R.color.semi_transparent_white
                )
            )
        }else{
            binding.tvChartsNoActivities.setTextColor(
                requireContext().getColorCompat(
                    R.color.semi_transparent_black
                )
            )
        }

        val activitiesViewModel = ViewModelProvider(this).get(ActivitiesViewModel::class.java)
        val activitiesListViewModel =
            ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

        val pieChart: PieChart = binding.pieChart
        val lineChart: LineChart = binding.lineChart

        // Initialize current month
        binding.chartsTvMonth.text = currentYearMonth.displayText()

        //disable arrow buttons
        binding.chartsArrowBackMonth.isEnabled = false
        binding.chartsArrowForwardMonth.isEnabled = false

        // Listener for the back-arrow month
        binding.chartsArrowBackMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            binding.chartsTvMonth.text = currentYearMonth.displayText()
            // update graph with new month data
            updatePieChart()
            updateLineChart()

            checkEnabledArrowBtns()
        }

        // Listener for the forward-arrow month
        binding.chartsArrowForwardMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            binding.chartsTvMonth.text = currentYearMonth.displayText()
            // Update graph with new month data
            updatePieChart()
            updateLineChart()

            checkEnabledArrowBtns()
        }


        activitiesViewModel.readAllData.observe(viewLifecycleOwner) { activities ->
            activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activitiesList ->
                //check if activities is not empty
                if (activities.isNotEmpty()) {
                    //check if activitiesList is not empty
                    if (activitiesList.isNotEmpty()) {
                        binding.tvChartsNoActivities.isVisible = false

                        this.activities = activities
                        this.activitiesList = activitiesList
                        createPieChart(pieChart, getCurrentMonthActivities())
                        createLineChart(lineChart, getCurrentMonthActivities())

                        setFirstAndLastYearMonth()
                    }
                } else {
                    binding.tvChartsNoActivities.isVisible = true
                }
            }
        }

        return root
    }


    private fun checkEnabledArrowBtns() {
        //Log.d("firstYearMonth.toString()", lastYearMonth.toString())

        if (currentYearMonth <= firstYearMonth) {
            binding.chartsArrowBackMonth.isEnabled = false
        }
        if (currentYearMonth < lastYearMonth) {
            binding.chartsArrowForwardMonth.isEnabled = true
        }
        if (currentYearMonth >= lastYearMonth) {
            binding.chartsArrowForwardMonth.isEnabled = false
        }
        if (currentYearMonth > firstYearMonth) {
            binding.chartsArrowBackMonth.isEnabled = true
        }
    }

    private fun updatePieChart() {
        val monthActivities = getCurrentMonthActivities()
        createPieChart(binding.pieChart, monthActivities)
    }

    private fun updateLineChart() {
        val monthActivities = getCurrentMonthActivities()
        createLineChart(binding.lineChart, monthActivities)
    }

    private fun getCurrentMonthActivities(): List<Activity> {
        return activities.filter { activity ->
            val activityStartDate = activity.startTime
            val activityEndDate = activity.stopTime

            val activityStartYearMonth = YearMonth.of(
                getYear(activityStartDate).toInt(),
                getMonth(activityStartDate).toInt()
            )
            val activityEndYearMonth = YearMonth.of(
                getYear(activityEndDate).toInt(),
                getMonth(activityEndDate).toInt()
            )

            activityStartYearMonth <= currentYearMonth && activityEndYearMonth >= currentYearMonth
        }
    }

    //if forward is true, check if there are activities after the current month, vice versa for false
    private fun setFirstAndLastYearMonth() {
        val activitiesViewModel = ViewModelProvider(this).get(ActivitiesViewModel::class.java)
        val activitiesListViewModel =
            ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

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

        combinedLiveData.observe(viewLifecycleOwner) { (firstActivity, latestActivity) ->
            if (firstActivity != null && latestActivity != null) {
                val startMonth = YearMonth.of(
                    getYear(firstActivity.startTime).toInt(),
                    getMonth(firstActivity.startTime).toInt()
                )
                val endMonth = YearMonth.of(
                    getYear(latestActivity.stopTime).toInt(),
                    getMonth(latestActivity.stopTime).toInt()
                )

                firstYearMonth = startMonth
                lastYearMonth = endMonth
                checkEnabledArrowBtns()

            }
        }

    }

    private fun createPieChart(pieChart: PieChart, activitiesMonth: List<Activity>) {
        val pieEntries = ArrayList<PieEntry>()

        for (activity in activitiesList) {
            val count = activitiesMonth.count { it.activityId == activity.id }

            //if some count is 0, don't add it to the pie chart
            if (count == 0) {
                continue
            }

            pieEntries.add(PieEntry(count.toFloat(), activity.name))
        }

        if (activitiesMonth.isNotEmpty() && pieEntries.size == 0) {
            pieChart.clear()
            pieChart.invalidate() // Refresh chart
            pieChart.setNoDataText("No activity found for this month.\nDid you deleted some of them?")
            pieChart.setNoDataTextColor(Color.BLACK)
            pieChart.description = null
            return
        }

        val dataSet = PieDataSet(pieEntries, "Activities")
        dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
        dataSet.valueTextSize = 16f

        dataSet.sliceSpace = 2f // spazio tra le sezioni del grafico
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() //no virgola
            }
        }

        pieChart.legend.textSize = 16f
        pieChart.legend.isEnabled = false

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.setEntryLabelTextSize(16f)
        pieChart.invalidate() // Refresh chart

        val description: Description = Description()
        description.text = "Activities desc"
        pieChart.description = description
        pieChart.description.isEnabled = false

    }

    private fun createLineChart(lineChart: LineChart, activitiesMonth: List<Activity>) {
        val lineEntriesMap = mutableMapOf<String, MutableList<Entry>>()
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        for (activity in activitiesList) {
            val count = activitiesMonth.count { it.activityId == activity.id }

            //if some count is 0, don't add it to the line chart
            if(count == 0){
                continue
            }

            //if activity has step == null, don't add it to the line chart
            //Log.d("activity.steps", activity.toString())
            if (activity.steps == null) {
                continue
            }
            lineEntriesMap[activity.name] = mutableListOf()
        }

        if(activitiesMonth.isNotEmpty() && lineEntriesMap.isEmpty()){
            lineChart.clear()
            lineChart.invalidate() // Refresh chart
            lineChart.setNoDataText("No step activity found for this month")
            lineChart.setNoDataTextColor(Color.BLACK)
            lineChart.description = null
            return
        }

        val daysInMonth = currentYearMonth.lengthOfMonth()

        for (day in 1..daysInMonth) {
            val date = currentYearMonth.atDay(day)
            val dayActivities = activitiesMonth.filter { activity ->
                val activityDate = Instant.ofEpochMilli(activity.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                activityDate == date
            }

            for (activity in activitiesList) {
                val stepsCount = dayActivities
                    .filter { it.activityId == activity.id }
                    .sumOf { it.steps?.toLong() ?: 0L }

                lineEntriesMap[activity.name]?.add(Entry(day.toFloat(), stepsCount.toFloat()))
            }
        }

        val lineDataSets = mutableListOf<LineDataSet>()
        for ((activityName, lineEntries) in lineEntriesMap) {
            if (lineEntries.isNotEmpty()) {
                val lineDataSet = LineDataSet(lineEntries, activityName)
                lineDataSet.color = ColorTemplate.COLORFUL_COLORS[lineDataSets.size % ColorTemplate.COLORFUL_COLORS.size]
                lineDataSet.valueTextSize = 12f
                lineDataSets.add(lineDataSet)
                lineDataSet.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString() //no virgola
                    }
                }
            }
        }

        lineChart.legend.textSize = 16f
        lineChart.legend.isEnabled = true

        val lineData = LineData(lineDataSets as List<ILineDataSet>?)
        lineChart.data = lineData
        lineChart.invalidate()

        val description: Description = Description()
        description.text = "Steps per day"
        lineChart.description = description
        lineChart.description.isEnabled = true
        lineChart.setBackgroundColor(Color.WHITE)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}