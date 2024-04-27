package com.example.personalphysicaltracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.personalphysicaltracker.R
import com.example.personalphysicaltracker.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //onclicklister
        binding.homeBtnStartAndStop.setOnClickListener {
            //when btn is clicked change src
            if (binding.homeBtnStartAndStop.tag == "start") {  //timer is stopped, we want to start it
                binding.homeBtnStartAndStop.setImageResource(R.drawable.round_stop_24)
                binding.homeBtnStartAndStop.tag = "stop"
            } else { //timer is running, we want to stop it
                binding.homeBtnStartAndStop.setImageResource(R.drawable.round_start_24)
                binding.homeBtnStartAndStop.tag = "start"
            }
        }

        //add activity button
        binding.homeBtnNewActivity.setOnClickListener {
            //navigate to add activity fragment
            findNavController().navigate(R.id.action_nav_home_to_addActivity)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}