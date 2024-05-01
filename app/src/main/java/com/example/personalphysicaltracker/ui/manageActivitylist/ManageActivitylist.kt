package com.example.personalphysicaltracker.ui.manageActivitylist

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.data.ActivitiesList
import com.example.personalphysicaltracker.data.ActivitiesListViewModel
import com.example.personalphysicaltracker.data.ExtraInfo
import com.example.personalphysicaltracker.databinding.FragmentManageActivitylistBinding
import com.example.personalphysicaltracker.ui.home.ManageActivitylistViewModel

class ManageActivitylist : Fragment() {
    private var _binding: FragmentManageActivitylistBinding? = null
    private val binding get() = _binding!!

    private lateinit var manageActivitylistViewModel: ManageActivitylistViewModel<Any?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val manageActivitylistViewModel =
            ViewModelProvider(this).get(ManageActivitylistViewModel::class.java)
        _binding = FragmentManageActivitylistBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //bindings
        val insertActivityBtn: Button = binding.insertActivityBtn
        insertActivityBtn.setOnClickListener { view ->
            insertDataToDatabase()
        }

        //recyclerview
        val adapter = ActivitiesListAdapter(
            { activitiesList -> manageActivitylistViewModel.editActivity(activitiesList) },
            { activitiesList -> manageActivitylistViewModel.deleteActivity(activitiesList) }
        )
        val recyclerView = binding.managerRvActivitiesList
        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        //ActivitiesListViewModel
        val activitiesListViewModel = ViewModelProvider(this).get(ActivitiesListViewModel::class.java)
        activitiesListViewModel.readAllData.observe(viewLifecycleOwner) { activitiesList ->
            adapter.setData(activitiesList)
        }

        return root
    }

    private fun insertDataToDatabase() {
        val activityName = binding.insertActivityNameEt.text.toString()

        if (inputCheck(activityName)) {
            //Create Object
            val newActivity = ActivitiesList(activityName, ExtraInfo(false, false, null, null))

            //Add Data to Database
            val activitiesListViewModel =
                ViewModelProvider(this).get(ActivitiesListViewModel::class.java)

            activitiesListViewModel.addActivity(newActivity)

            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()

            //clear fields
            binding.insertActivityNameEt.text.clear()
            //lower keyboard
            binding.insertActivityNameEt.clearFocus()
            hideKeyboard()

        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun inputCheck(userName: String): Boolean {
        //return false if any of the fields are empty
        return !(TextUtils.isEmpty(userName))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}