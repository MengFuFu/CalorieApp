package com.mh.calorie.ui.sport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentSportRecordBinding


/** 首页「运动」卡进来的运动记录页：目前只有空态 */
class SportRecordFragment : Fragment(R.layout.fragment_sport_record) {

    private var _binding: FragmentSportRecordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSportRecordBinding.bind(view)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvSync.setOnClickListener {
            Toast.makeText(requireContext(), "数据同步将在后续接入", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}