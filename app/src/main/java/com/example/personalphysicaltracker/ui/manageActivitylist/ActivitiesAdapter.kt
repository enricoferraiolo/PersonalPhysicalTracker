package com.example.personalphysicaltracker.ui.manageActivitylist

import android.app.AlertDialog
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesList
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivitiesListAdapter(
    private val editActivityCallback: (ActivitiesList) -> Unit,
    private val deleteActivityCallback: (ActivitiesList) -> Unit,
    private val context: Context

) : RecyclerView.Adapter<ActivitiesListAdapter.MyViewHolder>() {
    private var activitiesList = emptyList<ActivitiesList>()

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
        val nameTextView = holder.itemView.findViewById<TextView>(R.id.name_list_frag)
        nameTextView.setText(currentItem.name)
        //nameTextView.isEnabled = false
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


        //set on click listener for each item
        btnEdit.setOnClickListener {
            showEditDialog(currentItem)
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog(currentItem)
        }
    }

    private fun showDeleteConfirmationDialog(currentItem: ActivitiesList) {
        val message = "Are you sure you want to delete <i><b>${currentItem.name}</b></i> activity?"
        AlertDialog.Builder(context)
            .setTitle("Confirm Delete")
            .setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Yes") { dialog, _ ->
                deleteActivity(currentItem)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteActivity(currentItem: ActivitiesList) {
        deleteActivityCallback(currentItem)
        notifyDataSetChanged()
    }

    private fun showEditDialog(currentItem: ActivitiesList) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_activity, null)
        val editText = dialogView.findViewById<EditText>(R.id.edit_activity_name)
        editText.setText(currentItem.name)

        AlertDialog.Builder(context)
            .setTitle("Edit Activity")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                currentItem.name = editText.text.toString()
                editActivityCallback(currentItem)
                notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun getItemCount(): Int {
        return activitiesList.size
    }

    fun setData(activitiesList: List<ActivitiesList>) {
        this.activitiesList = activitiesList
        notifyDataSetChanged() //notify recyclerview that data has changed
    }
}