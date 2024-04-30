package com.example.personalphysicaltracker.data.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesList

class ListAdapter : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    /*private var userList = emptyList<User>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.custom_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_frag)?.text =
            currentItem.id.toString()
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_list_frag)?.text =
            currentItem.name
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setData(user: List<User>) {
        this.userList = user
        notifyDataSetChanged() //notify recyclerview that data has changed
    }*/

    private var activitiesList = emptyList<ActivitiesList>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.custom_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListAdapter.MyViewHolder, position: Int) {
        val currentItem = activitiesList[position]
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.id_list_frag)?.text = position.toString()
        holder.itemView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.name_list_frag)?.text =
            currentItem.name
    }

    override fun getItemCount(): Int {
        return activitiesList.size
    }

    fun setData(activitiesList: List<ActivitiesList>) {
        this.activitiesList = activitiesList
        notifyDataSetChanged() //notify recyclerview that data has changed
    }

}