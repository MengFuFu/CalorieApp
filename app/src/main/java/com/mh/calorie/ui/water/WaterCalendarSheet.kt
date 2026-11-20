package com.mh.calorie.ui.water

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import com.mh.calorie.R
import com.mh.calorie.data.temp.TempWaterRepository
import com.mh.calorie.databinding.SheetWaterCalendarBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * 喝水日历：按月展示，每天一个水杯，水位 = 当天净水量 ÷ 目标。
 * 点某天回传日期，首页切到那天。
 */
@AndroidEntryPoint
class WaterCalendarSheet : DialogFragment() {

    private var _binding: SheetWaterCalendarBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repo: TempWaterRepository

    private var month: YearMonth = YearMonth.now()

    private val adapter = WaterCalendarAdapter { cell ->
        val iso = cell.dateIso ?: return@WaterCalendarAdapter
        setFragmentResult(REQUEST_KEY, bundleOf(RESULT_DATE to iso))
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 透明底 + 自定义进出场动画都由这个主题提供
        setStyle(STYLE_NO_TITLE, R.style.Theme_Momo_TopSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetWaterCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), COLUMNS)
        binding.rvCalendar.adapter = adapter

        binding.btnPrevMonth.setOnClickListener {
            month = month.minusMonths(1)
            render()
        }
        binding.btnNextMonth.setOnClickListener {
            if (month < YearMonth.now()) {
                month = month.plusMonths(1)
                render()
            }
        }
        binding.btnBackToday.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_DATE to LocalDate.now().toString()))
            dismiss()
        }

        render()
    }

    override fun onStart() {
        super.onStart()
        // 原来那段 BottomSheetBehavior 全部删掉，换成顶部对齐
        val window = dialog?.window ?: return
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
    }

    private fun render() {
        binding.tvMonth.text = month.format(MONTH_FORMAT)
        binding.btnNextMonth.isEnabled = month < YearMonth.now()
        binding.btnNextMonth.alpha = if (binding.btnNextMonth.isEnabled) 1f else 0.3f

        val today = LocalDate.now()
        // 周一为第一列：DayOfWeek 里周一 = 1，所以 (value - 1) 就是前导空格数
        val leading = month.atDay(1).dayOfWeek.value - 1
        val days = month.lengthOfMonth()

        val cells = (0 until ROWS * COLUMNS).map { index ->
            val day = index - leading + 1
            if (day !in 1..days) {
                WaterDayCell()
            } else {
                val date = month.atDay(day)
                val future = date.isAfter(today)
                WaterDayCell(
                    day = day,
                    ratio = if (future) 0f else repo.ratioOn(date),
                    isToday = date == today,
                    dateIso = if (future) null else date.toString(),
                    enabled = !future
                )
            }
        }
        adapter.submitList(cells)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCalendar.adapter = null
        _binding = null
    }

    companion object {
        const val TAG = "WaterCalendarSheet"
        const val REQUEST_KEY = "water_calendar_request"
        const val RESULT_DATE = "water_calendar_date"

        private const val COLUMNS = 7
        private const val ROWS = 6
        private val MONTH_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
    }
}