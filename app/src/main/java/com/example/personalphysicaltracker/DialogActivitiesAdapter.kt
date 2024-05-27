package com.example.personalphysicaltracker

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.data.ActivitiesList


class DialogActivitiesAdapter(
    private val activitiesList: List<ActivitiesList>,
    private val filterActivities: List<ActivitiesList>
) : RecyclerView.Adapter<DialogActivitiesAdapter.ViewHolder>() {

    private val selectedActivities = filterActivities.toMutableList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_activity_dialog_rv)

        fun bind(activity: ActivitiesList) {
            checkBox.text = activity.name
            checkBox.isChecked = filterActivities.contains(activity)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedActivities.add(activity)
                } else {
                    selectedActivities.remove(activity)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_checkbox, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activitiesList[position])
    }

    override fun getItemCount(): Int {
        return activitiesList.size
    }

    fun getSelectedActivities(): List<ActivitiesList> {
        return selectedActivities
    }
}
