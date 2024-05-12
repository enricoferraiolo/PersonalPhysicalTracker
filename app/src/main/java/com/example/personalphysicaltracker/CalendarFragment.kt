package com.example.personalphysicaltracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.personalphysicaltracker.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.Locale


class CalendarFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var _binding: FragmentCalendarBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val calendarView = binding.calendarView
        val dateStr = "06/05/2024"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(dateStr)
        date?.time?.let { calendarView.date = it }

        return root
    }


}