package com.example.personalphysicaltracker

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.personalphysicaltracker.data.User
import com.example.personalphysicaltracker.data.UserViewModel
import java.lang.Double
import kotlin.Boolean
import kotlin.String
import kotlin.toString

class RegistrationActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        //get the view model
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        findViewById<Button>(R.id.registration_register_btn).setOnClickListener {
            //push to database
            insertDataToDatabase()

            //redirect to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun insertDataToDatabase() {
        val firstName =
            findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.registration_name_et)?.text.toString()
        val age =
            findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.registration_age_et)?.text.toString()
        val height =
            findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.registration_height_et)?.text.toString()
        val weight =
            findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.registration_weight_et)?.text.toString()

        if (inputCheck(firstName, age, height, weight)) {
            //Create User Object
            val user = User(
                0,
                firstName,
                Integer.parseInt(age),
                Double.parseDouble(weight),
                Double.parseDouble(height)
            )

            //Add Data to Database
            userViewModel.addUser(user)
            Toast.makeText(this, "Successfully added!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_LONG)
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
