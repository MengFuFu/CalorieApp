package com.mh.calorie.ui.sport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentSportRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


/** 运动记录页：按月份筛选、按日期分组 */
@AndroidEntryPoint
class SportRecordFragment : Fragment(R.layout.fragment_sport_record) {

    private var _binding: FragmentSportRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SportRecordViewModel by viewModels()
    private val adapter = SportRecordAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSportRecordBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvSync.setOnClickListener {
            Toast.makeText(requireContext(), "数据同步将在后续接入", Toast.LENGTH_SHORT).show()
        }
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
        binding.btnGoRecord.setOnClickListener {
            findNavController().navigate(R.id.action_sportRecordFragment_to_sportLibraryFragment)
        }

        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: SportRecordUiState) {
        binding.tvMonth.text = state.monthText
        binding.tvMonthTotal.text = getString(R.string.sport_month_total, state.totalText)
        binding.btnNextMonth.isEnabled = state.canGoNext
        binding.btnNextMonth.alpha = if (state.canGoNext) 1f else 0.3f

        binding.groupEmpty.isVisible = state.isEmpty
        binding.rvRecords.isVisible = !state.isEmpty
        adapter.submitList(state.groups)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvRecords.adapter = null
        _binding = null
    }

}