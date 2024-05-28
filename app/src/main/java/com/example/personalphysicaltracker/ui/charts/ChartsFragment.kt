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
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ActivitiesViewModel
import com.example.personalphysicaltracker.data.Activity
import com.example.personalphysicaltracker.databinding.FragmentChartsBinding
import com.example.personalphysicaltracker.displayText
import com.example.personalphysicaltracker.getMonth
import com.example.personalphysicaltracker.getYear
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.DateValidatorPointForward
import java.time.YearMonth


class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        val activitiesViewModel = ViewModelProvider(this).get(ActivitiesViewModel::class.java)
        val activitiesListViewModel =
            ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

        val pieChart: PieChart = binding.pieChart

        // Inizializzazione del mese visualizzato
        binding.chartsTvMonth.text = currentYearMonth.displayText()


        // Listener per il cambio mese indietro
        binding.chartsArrowBackMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.minusMonths(1)
            binding.chartsTvMonth.text = currentYearMonth.displayText()
            // Aggiorna il grafico con i dati relativi al nuovo mese
            updatePieChart()

            checkEnabledArrowBtns()
        }

        // Listener per il cambio mese avanti
        binding.chartsArrowForwardMonth.setOnClickListener {
            currentYearMonth = currentYearMonth.plusMonths(1)
            binding.chartsTvMonth.text = currentYearMonth.displayText()
            // Aggiorna il grafico con i dati relativi al nuovo mese
            updatePieChart()

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

                        setFirstAndLastYearMonth()
                    }
                } else {
                    binding.tvChartsNoActivities.isVisible = true
                }
            }
        }


        val pieEntries = ArrayList<PieEntry>()

        for (activity in activitiesList) {
            val count = activities.count { it.activityId == activity.id }
            pieEntries.add(PieEntry(count.toFloat(), activity.name))
        }

        val dataSet = PieDataSet(pieEntries, "Activities")
        dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // Refresh chart

        val description: Description = Description()
        description.text = "Activities"
        pieChart.description = description


        return root
    }


    private fun checkEnabledArrowBtns() {

        Log.d("firstYearMonth.toString()", lastYearMonth.toString())
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

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // Refresh chart

        val description: Description = Description()
        description.text = "Activities"
        pieChart.description = description
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}