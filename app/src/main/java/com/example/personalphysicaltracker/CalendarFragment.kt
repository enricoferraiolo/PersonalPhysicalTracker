package com.example.personalphysicaltracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.personalphysicaltracker.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CalendarFragment : Fragment(), DatePickerFragment.OnDateSetListener {

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

        binding.btnPickDate.setOnClickListener { view ->
            val newFragment = DatePickerFragment()
            newFragment.listener = this
            newFragment.show(parentFragmentManager, "datePicker")
        }

        // Imposta la data odierna nel TextView
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = dateFormat.format(calendar.time)
        binding.tvSelectedDate.text = todayDate

        return root
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        val selectedDate = "$day/${month + 1}/$year"
        binding.tvSelectedDate.text = selectedDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}