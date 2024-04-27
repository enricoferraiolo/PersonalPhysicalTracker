package com.example.personalphysicaltracker.data.fragments.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.ActivitiesListViewModel

class ListFragment : Fragment() {
    /*private lateinit var mUserViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        //add user button
        view.findViewById<View>(R.id.floatingActionButton_list).setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        //clear db button
        view.findViewById<View>(R.id.clear_db_btn).setOnClickListener {
            mUserViewModel.resetDB()
        }


        //RecyclerView
        val adapter = ListAdapter()
        val recyclerView =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        //UserViewModel
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mUserViewModel.readAllData.observe(viewLifecycleOwner, Observer { user ->
            adapter.setData(user)
        })

        return view
    }*/

    private lateinit var mActivitiesListViewModel: ActivitiesListViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        //add activity button
        view.findViewById<View>(R.id.floatingActionButton_list).setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        //RecyclerView
        val adapter = ListAdapter()
        val recyclerView =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        //ActivitiesListViewModel
        mActivitiesListViewModel = ViewModelProvider(this).get(ActivitiesListViewModel::class.java)
        mActivitiesListViewModel.readAllData.observe(viewLifecycleOwner, Observer { activitiesList ->
            adapter.setData(activitiesList)
        })

        return view
    }
}