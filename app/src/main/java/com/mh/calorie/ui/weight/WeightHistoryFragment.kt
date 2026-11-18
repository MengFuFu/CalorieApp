package com.mh.calorie.ui.weight

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentWeightHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** 「查看更多」进来的历史体重：按月份翻页，一天一张卡 */
@AndroidEntryPoint
class WeightHistoryFragment : Fragment(R.layout.fragment_weight_history) {

    private var _binding: FragmentWeightHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeightHistoryViewModel by viewModels()
    private val adapter = WeightHistoryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentWeightHistoryBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
        // 空态的「去记录」= 退回记录页
        binding.btnGoRecord.setOnClickListener { findNavController().navigateUp() }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: WeightHistoryUiState) {
        binding.tvMonth.text = state.monthText
        adapter.submitList(state.groups)

        binding.groupEmpty.isVisible = state.isEmpty
        binding.rvHistory.isVisible = !state.isEmpty

        // 已经是当前月，右箭头置灰
        binding.btnNextMonth.isEnabled = state.canGoNext
        binding.btnNextMonth.alpha = if(state.canGoNext) 1f else 0.35f
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvHistory.adapter = null
        _binding = null
    }
}