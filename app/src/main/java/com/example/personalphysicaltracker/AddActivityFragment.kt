package com.example.personalphysicaltracker

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ExtraInfo




/**
 * A simple [Fragment] subclass.
 * Use the [AddActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddActivityFragment : Fragment() {
    private lateinit var mActivitiesListViewModel: ActivitiesListViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_activity, container, false)
        mActivitiesListViewModel = ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

        view.findViewById<Button>(R.id.add_insert_activity_btn).setOnClickListener {
            insertDataToDatabase()
        }

        return view
    }

    private fun insertDataToDatabase() {
        val activityName =
            view?.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.add_activity_name_et)?.text.toString()

        if (inputCheck(activityName)) {
            //Create User Object
            val activity = ActivitiesList(
                0,
                activityName,
                ExtraInfo(10, 2.0)
            )

            //Add Data to Database
            mActivitiesListViewModel.addActivity(activity)
            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()

            //Navigate Back
            findNavController().navigate(R.id.action_addActivity_to_nav_home)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun inputCheck(activityName: String): Boolean {
        //return false if any of the fields are empty
        return !(TextUtils.isEmpty(activityName))
    }
}