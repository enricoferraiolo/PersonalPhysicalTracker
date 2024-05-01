package com.example.personalphysicaltracker.ui.manageActivitylist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivitiesListAdapter(
    private val editActivityCallback: (ActivitiesList) -> Unit,
    private val deleteActivityCallback: (ActivitiesList) -> Unit
) : RecyclerView.Adapter<ActivitiesListAdapter.MyViewHolder>() {
    private var activitiesList = emptyList<ActivitiesList>()
    private lateinit var activitiesListViewModel: ActivitiesListViewModel

    private var editing = false
    private var lastBtn: FloatingActionButton? = null

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ActivitiesListAdapter.MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_activities_manager, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ActivitiesListAdapter.MyViewHolder, position: Int) {
        val currentItem = activitiesList[position]
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_frag)?.text =
            position.toString()
        val nameTextView = holder.itemView.findViewById<EditText>(R.id.name_list_frag)
        nameTextView.setText(currentItem.name)
        nameTextView.isEnabled = false
        nameTextView.isFocusable = false
        nameTextView.isFocusableInTouchMode = false


        //if item is in default list, disable delete button
        if (currentItem.isDefault == true) {
            holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_delete).isEnabled =
                false
            holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_edit).isEnabled =
                false

            Log.d("ActivitiesListAdapter", "default item: ${currentItem.name} IS DEFAULT")
        }

        //set on click listener for each item
        holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_edit)
            .setOnClickListener {
                editActivity(
                    holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_edit),
                    nameTextView,
                    currentItem
                )
            }

        holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_delete)
            .setOnClickListener {
                deleteActivity(currentItem)
            }
    }

    private fun deleteActivity(currentItem: ActivitiesList) {
        deleteActivityCallback(currentItem)
        notifyDataSetChanged()
    }

    private fun editActivity(
        btn: FloatingActionButton,
        nameTextView: EditText,
        currentItem: ActivitiesList
    ) {

    }




    override fun getItemCount(): Int {
        return activitiesList.size
    }

    fun setData(activitiesList: List<ActivitiesList>) {
        this.activitiesList = activitiesList
        notifyDataSetChanged() //notify recyclerview that data has changed
    }

    private fun changeViewSrc(button: FloatingActionButton, drawable: Int) {
        button.setImageResource(drawable)
    }

    private fun resetEditBtn(button: FloatingActionButton) {
        button.setImageResource(R.drawable.round_edit_24)
        editing = false
    }
}