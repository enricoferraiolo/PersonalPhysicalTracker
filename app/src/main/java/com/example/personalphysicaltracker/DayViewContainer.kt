package com.example.personalphysicaltracker

import android.view.View
import android.widget.TextView
import com.example.personalphysicaltracker.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
}