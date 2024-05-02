package com.example.personalphysicaltracker.ui.manageActivitylist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesList
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivitiesListAdapter(
    private val editActivityCallback: (ActivitiesList) -> Unit,
    private val deleteActivityCallback: (ActivitiesList) -> Unit
) : RecyclerView.Adapter<ActivitiesListAdapter.MyViewHolder>() {
    private var activitiesList = emptyList<ActivitiesList>()
    private var editing = false
    private var lastItem: ActivitiesList? = null
    private var lastEditBtn: FloatingActionButton? = null
    private var lastNameET: EditText? = null

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
            currentItem.id.toString()
        val nameTextView = holder.itemView.findViewById<EditText>(R.id.name_list_frag)
        nameTextView.setText(currentItem.name)
        nameTextView.isEnabled = false
        nameTextView.isFocusable = false
        nameTextView.isFocusableInTouchMode = false

        val btnEdit = holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_edit)
        val btnDelete =
            holder.itemView.findViewById<FloatingActionButton>(R.id.custom_row_btn_delete)

        btnDelete.isEnabled = true
        btnEdit.isEnabled = true

        //if item is in default list, disable delete button
        if (currentItem.isDefault == true) {
            btnDelete.isEnabled = false
            btnEdit.isEnabled = false
        }

        //reset last items
        setLastItems(null, null, null)

        //reset editing
        resetEditBtn(btnEdit)


        //set on click listener for each item
        btnEdit.setOnClickListener {
            editActivity(
                btnEdit,
                nameTextView,
                currentItem
            )
        }

        btnDelete.setOnClickListener {
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
        if (lastItem != null && lastItem != currentItem) {
            resetEditBtn(lastEditBtn!!)
            changeETStatus(lastNameET!!, false)
        }

        if (!editing) {//if not editing, change to editing mode
            editing = true
            changeViewSrc(btn, R.drawable.round_check_24)
            changeETStatus(nameTextView, true)
            setLastItems(currentItem, btn, nameTextView)
        } else {    //if editing, save changes
            editing = false
            changeViewSrc(btn, R.drawable.round_edit_24)
            changeETStatus(nameTextView, false)
            currentItem.name = nameTextView.text.toString()
            editActivityCallback(currentItem)
            setLastItems(null, null, null)
        }
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

    private fun changeETStatus(editText: EditText, status: Boolean) {
        editText.isEnabled = status
        editText.isFocusable = status
        editText.isFocusableInTouchMode = status
    }

    private fun setLastItems(item: ActivitiesList?, btn: FloatingActionButton?, et: EditText?) {
        lastItem = item
        lastEditBtn = btn
        lastNameET = et
    }
}