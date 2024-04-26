package com.example.personalphysicaltracker.data.fragments.add

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
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.data.User
import com.example.personalphysicaltracker.data.UserViewModel


class AddFragment : Fragment() {
    private lateinit var mUserViewModel: UserViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        view.findViewById<Button>(R.id.button_add_user).setOnClickListener {
            insertDataToDatabase()
        }

        return view
    }

    private fun insertDataToDatabase() {
        val firstName =
            view?.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.name_et_list)?.text.toString()
        val age =
            view?.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.age_et_list)?.text.toString()
        val height =
            view?.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.height_et_list)?.text.toString()
        val weight =
            view?.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.weight_et_list)?.text.toString()

        if (inputCheck(firstName, age, height, weight)) {
            //Create User Object
            val user = User(
                0,
                firstName,
                Integer.parseInt(age),
                java.lang.Double.parseDouble(weight),
                java.lang.Double.parseDouble(height)
            )

            //Add Data to Database
            mUserViewModel.addUser(user)
            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()
            //Navigate Back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun inputCheck(
        firstName: String,
        age: String,
        height: String,
        weight: String
    ): Boolean {
        //return false if any of the fields are empty
        return !(TextUtils.isEmpty(firstName) && TextUtils.isEmpty(age) && TextUtils.isEmpty(height) && TextUtils.isEmpty(
            weight
        ))
    }
}