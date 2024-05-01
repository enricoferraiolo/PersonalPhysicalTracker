package com.example.personalphysicaltracker.ui.manageActivitylist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesList
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivitiesListAdapter: RecyclerView.Adapter<ActivitiesListAdapter.MyViewHolder>() {
    private var activitiesList = emptyList<ActivitiesList>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesListAdapter.MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.custom_row_activities_manager, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ActivitiesListAdapter.MyViewHolder, position: Int) {
        val currentItem = activitiesList[position]
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_frag)?.text =
            position.toString()
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_list_frag)?.text =
            currentItem.name

        //set on click listener for each item
        holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_edit).setOnClickListener {
            editActivity(currentItem)
        }

        holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_delete).setOnClickListener {
            deleteActivity(currentItem)
        }
    }

    private fun deleteActivity(currentItem: ActivitiesList) {
        Log.d("ActivitiesListAdapter", "deleteActivity: $currentItem")

    }

    private fun editActivity(currentItem: ActivitiesList) {
        Log.d("ActivitiesListAdapter", "editActivity: $currentItem")
    }

    override fun getItemCount(): Int {
        return activitiesList.size
    }

    fun setData(activitiesList: List<ActivitiesList>) {
        this.activitiesList = activitiesList
        notifyDataSetChanged() //notify recyclerview that data has changed
    }
}