package com.mh.calorie.ui.weight.input

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mh.calorie.R
import com.mh.calorie.databinding.SheetDateWheelBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 日期滚轮面板：日 / 时 / 分三列。
 *
 * 从 WeightInputSheet 用 childFragmentManager 弹出来，所以输入面板会留在后面，
 * 和设计稿那种「第二层 sheet 压在第一层上」一致。
 */
class DateWheelSheet  : BottomSheetDialogFragment() {

    private var _binding: SheetDateWheelBinding? = null
    private val binding get() = _binding!!

    /** 可选日期：最近 90 天，最后一项是今天（不允许记未来） */
    private val days = mutableListOf<LocalDate>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SheetDateWheelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val initial = arguments?.getString(ARG_DATETIME)
            ?.let { runCatching { LocalDateTime.parse(it, FORMATTER) }.getOrNull() }
            ?: LocalDateTime.now()

        buildDays(initial.toLocalDate())

        val dayLabels = days.map { day ->
            if(day == LocalDate.now()) getString(R.string.date_today)
            else day.format(DAY_FORMATTER)
        }
        val hourLabels = (0..23).map { getString(R.string.hour_format, it) }
        val minuteLabels = (0..59).map { getString(R.string.minute_format, it) }

        binding.wheelDay.setItems(dayLabels, days.indexOf(initial.toLocalDate()).coerceAtLeast(0))
        binding.wheelHour.setItems(hourLabels, initial.hour)
        binding.wheelMinute.setItems(minuteLabels, initial.minute)

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener { confirm() }
    }

    override fun onStart() {
        super.onStart()
        // 和输入面板同样处理：换掉 Material 自带的直角背景
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(R.drawable.bg_sheet_white)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    /** 生成「今天往前 90 天」，若传入日期更早则单独补在最前面 */
    private fun buildDays(selected: LocalDate) {
        val today = LocalDate.now()
        val start = today.minusDays(89)
        days.clear()
        var cursor = start
        while (!cursor.isAfter(today)) {
            days += cursor
            cursor = cursor.plusDays(1)
        }
        if(selected.isBefore(start)) days.add(0, selected)
    }

    private fun confirm() {
        val date = days.getOrNull(binding.wheelDay.selectedIndex) ?: return
        // 小时、分钟的滚轮下标就是数值本身，直接用
        val time = LocalTime.of(binding.wheelHour.selectedIndex, binding.wheelMinute.selectedIndex)

        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(RESULT_DATETIME to date.atTime(time).format(FORMATTER))
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DateWheelSheet"
        const val REQUEST_KEY = "date_wheel_request"
        const val RESULT_DATETIME = "date_wheel_value"

        /** 与 WeightInputSheet 共用同一种时间格式 */
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        private const val ARG_DATETIME = "arg_datetime"
        private val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d", Locale.US)

        fun newInstance(current: LocalDateTime): DateWheelSheet = DateWheelSheet().apply {
            arguments = bundleOf(ARG_DATETIME to current.format(FORMATTER))
        }
    }
}