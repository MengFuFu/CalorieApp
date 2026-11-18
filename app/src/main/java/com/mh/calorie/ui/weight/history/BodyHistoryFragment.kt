package com.mh.calorie.ui.weight.history

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
import com.mh.calorie.databinding.FragmentBodyHistoryBinding
import com.mh.calorie.ui.weight.body.BodyUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** 「历史围度」右上角查看更多进来的页面 */
@AndroidEntryPoint
class BodyHistoryFragment : Fragment(R.layout.fragment_body_history) {

    private var _binding: FragmentBodyHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BodyHistoryViewModel by viewModels()
    private val adapter = BodyHistoryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentBodyHistoryBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
        // 空态「去记录」= 退回记录页，让用户去点「记录围度」
        binding.btnGoRecord.setOnClickListener { findNavController().navigateUp() }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: BodyHistoryUiState) {
        binding.tvMonth.text = state.monthText
        adapter.submitList(state.groups)

        binding.groupEmpty.isVisible = state.isEmpty
        binding.rvHistory.isVisible = !state.isEmpty

        binding.btnNextMonth.isEnabled = state.canGoNext
        binding.btnNextMonth.alpha = if (state.canGoNext) 1f else 0.35f
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvHistory.adapter = null
        _binding = null
    }
}