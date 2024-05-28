package com.example.personalphysicaltracker.ui.charts

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.databinding.FragmentChartsBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate


class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val chartsViewModel =
            ViewModelProvider(this).get(ChartsViewModel::class.java)

        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //piechart
        val pieChart: PieChart = binding.pieChart

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(40f, "January"))
        entries.add(PieEntry(30f, "February"))
        entries.add(PieEntry(20f, "March"))
        entries.add(PieEntry(10f, "April"))

        val dataSet = PieDataSet(entries, "Monthly Sales")
        dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // Refresh chart

        val description: Description = Description()
        description.text = "Sales by Month"
        pieChart.description = description

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}