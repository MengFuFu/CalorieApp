package com.mh.calorie.ui.stats

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.FoodLog
import com.mh.calorie.data.temp.TempFoodLogRepository
import com.mh.calorie.databinding.FragmentLifeFoodDetailBinding
import com.mh.calorie.ui.common.labelRes
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** 生活美食「全部」：选一天，横滑看这天的每一餐 */
@AndroidEntryPoint
class LifeFoodDetailFragment : Fragment(R.layout.fragment_life_food_detail) {

    @Inject
    lateinit var foodRepo: TempFoodLogRepository

    private var _binding: FragmentLifeFoodDetailBinding? = null
    private val binding get() = _binding!!

    private val adapter = LifeFoodCardAdapter()
    private lateinit var date: LocalDate
    private var logs: List<FoodLog> = emptyList()

    private var startIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLifeFoodDetailBinding.bind(view)

        startIndex = arguments?.getInt(ARG_INDEX) ?: 0
        date = arguments?.getString(ARG_DATE)?.let { LocalDate.parse(it) } ?: LocalDate.now()
        logs = foodRepo.ofDate(date).sortedBy { it.recordedAt }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnMore.setOnClickListener {
            Toast.makeText(requireContext(), R.string.stats_life_more_coming, Toast.LENGTH_SHORT).show()
        }

        binding.pager.adapter = adapter
        adapter.submit(logs)
        // 有传入下标就直接定位到那张卡（false = 不要滑动动画）
        if (logs.isNotEmpty()) {
            binding.pager.setCurrentItem(startIndex.coerceIn(0, logs.lastIndex), false)
        }
        renderHeader(startIndex.coerceIn(0, logs.lastIndex.coerceAtLeast(0)))

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = renderHeader(position)
        })
        renderHeader(0)
    }

    /** 标题跟着当前卡变成「2026.11.23加餐」，底部页码同步 */
    private fun renderHeader(position: Int) {
        val current = logs.getOrNull(position)
        binding.tvTitle.text = buildString {
            append(date.format(HEADER_FORMAT))
            current?.let { append(getString(it.meal.labelRes)) }
        }
        binding.tvIndicator.text =
            getString(R.string.stats_life_page, position + 1, logs.size)
        binding.tvIndicator.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.pager.adapter = null
        _binding = null
    }

    companion object {
        const val ARG_DATE = "life_detail_date"
        const val ARG_INDEX = "life_index"
        private val HEADER_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.US)
    }
}