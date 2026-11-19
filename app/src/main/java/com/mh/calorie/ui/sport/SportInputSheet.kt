package com.mh.calorie.ui.sport

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mh.calorie.R
import com.mh.calorie.data.temp.TempSportRecordRepository
import com.mh.calorie.databinding.SheetSportInputBinding
import com.mh.calorie.ui.weight.input.DateWheelSheet
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * 运动库点「＋」弹出的记录面板：
 * 日期胶囊 + 所选运动 + 大数字 + 单位（min / km / 局(x min) / 场(x min) / 个(x/分钟)）+ 自绘键盘。
 * 保存后通过 Fragment Result 把「千卡 + 数量」回传给运动库页。
 */
@AndroidEntryPoint
class SportInputSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var sportRepo: TempSportRecordRepository

    private var _binding: SheetSportInputBinding? = null
    private val binding get() = _binding!!

    private var selectedAt: LocalDateTime = LocalDateTime.now()
    private var unit: SportUnit = SportUnit.MIN
    private val input = StringBuilder()

    private val itemName: String get() = requireArguments().getString(ARG_NAME).orEmpty()
    private val kcalPer10: Float get() = requireArguments().getFloat(ARG_KCAL, 0f)

    /** 第二单位：km / 局 / 场 / 个，由运动库按类型传进来 */
    private val secondUnit: SportUnit
        get() = runCatching {
            SportUnit.valueOf(requireArguments().getString(ARG_UNIT) ?: SportUnit.SESSION.name)
        }.getOrDefault(SportUnit.SESSION)

    /** 10 分钟里有几个第二单位 */
    private val per10: Float get() = requireArguments().getFloat(ARG_PER10, 1f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SheetSportInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvSportName.text = itemName
        binding.tvSportKcal.text = formatNumber(kcalPer10)

        // 日期胶囊 → 复用体重页那套日期滚轮
        binding.tvDate.setOnClickListener {
            DateWheelSheet.newInstance(selectedAt).show(childFragmentManager, DateWheelSheet.TAG)
        }
        childFragmentManager.setFragmentResultListener(
            DateWheelSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(DateWheelSheet.RESULT_DATETIME)
                ?: return@setFragmentResultListener
            selectedAt = runCatching { LocalDateTime.parse(text, DateWheelSheet.FORMATTER) }
                .getOrElse { LocalDateTime.now() }
            renderDate()
        }

        binding.btnClose.setOnClickListener { dismiss() }

        // 单位：min 恒定，第二个按运动类型
        binding.tvUnit1.setOnClickListener { switchUnit(SportUnit.MIN) }
        binding.tvUnit2.setOnClickListener { switchUnit(secondUnit) }

        keyViews().forEachIndexed { index, key -> key.setOnClickListener { onDigit(index) } }
        binding.btnDelete.setOnClickListener { onDelete() }
        binding.btnSave.setOnClickListener { save() }

        renderDate()
        switchUnit(SportUnit.MIN)
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(R.drawable.bg_sheet_white)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── 键盘 ──────────────────────────────────────────────

    /** key0..key9，顺序即数字顺序 */
    private fun keyViews(): List<TextView> = listOf(
        binding.key1, binding.key2, binding.key3,
        binding.key4, binding.key5, binding.key6,
        binding.key7, binding.key8, binding.key9,
        binding.key0
    )

    private fun onDigit(index: Int) {
        if(input.length >= MAX_LENGTH) {
            toast(R.string.sport_value_too_long)
            return
        }
        val digit = if (index == 9) 0 else index + 1
        // 已有单个 0 时，新数字顶掉它
        if (input.length == 1 && input[0] == '0') input.clear()
        input.append(digit)
        renderValue()
    }

    private fun onDelete() {
        if (input.isNotEmpty()) input.deleteCharAt(input.length - 1)
        renderValue()
    }

    // ── 单位与渲染 ─────────────────────────────────────────

    private fun switchUnit(target: SportUnit) {
        unit = target
        input.clear()
        input.append(DEFAULT_VALUE[target] ?: 1)
        renderUnits()
        renderValue()
    }

    private fun renderUnits() {
        val ctx = requireContext()
        val selected = ContextCompat.getColor(ctx, R.color.brand_green)
        val normal = ContextCompat.getColor(ctx, R.color.text_tertiary)

        binding.tvUnit1.text = getString(SportUnit.MIN.labelRes)
        binding.tvUnit1.setTextColor(if (unit == SportUnit.MIN) selected else normal)
        binding.tvUnit1.setTypeface(null, if (unit == SportUnit.MIN) Typeface.BOLD else Typeface.NORMAL)

        binding.tvUnit2.text = labelOf(secondUnit)
        binding.tvUnit2.setTextColor(if (unit == secondUnit) selected else normal)
        binding.tvUnit2.setTypeface(null, if (unit == secondUnit) Typeface.BOLD else Typeface.NORMAL)
    }

    /** 单位文案：场(40min) / 局(20min) / 个(20/分钟) / km / min */
    private fun labelOf(target: SportUnit): String = when(target) {
        SportUnit.MIN -> getString(R.string.sport_unit_min)
        SportUnit.KM -> getString(R.string.sport_unit_km)
        SportUnit.ROUND -> getString(R.string.sport_unit_round_with_time, minutesPerUnit())
        SportUnit.SESSION -> getString(R.string.sport_unit_session_with_time, minutesPerUnit())
        SportUnit.REP -> getString(R.string.sport_unit_rep_with_rate, repsPerMinute())
    }

    /** 一局/一场多少分钟 */
    private fun minutesPerUnit(): Int = if (per10 > 0f) (10f / per10).roundToInt() else 0

    /** 一分钟多少个 */
    private fun repsPerMinute(): Int = (per10 / 10f).roundToInt()

    private fun renderDate() {
        binding.tvDateText.text = selectedAt.format(DATE_FORMATTER)
    }

    private fun renderValue() {
        binding.tvValue.text = input.toString().ifEmpty { "0" }
        val value = input.toString().toFloatOrNull() ?: 0f
        binding.tvKcal.text =  "${formatNumber(kcalOf(value, unit))} kcal"
    }

    // ── 换算 & 保存 ────────────────────────────────────────

    /** 与 SportItem.kcalFor 完全一致 */
    private fun kcalOf(value: Float, target: SportUnit): Float = when {
        target == SportUnit.MIN -> kcalPer10 * value / 10f
        per10 <= 0f -> 0f
        else -> kcalPer10 * value / per10
    }

    private fun save() {
        val value = input.toString().toFloatOrNull() ?: 0f
        if (value <= 0f) {
            toast(R.string.sport_value_empty)
            return
        }
        val kcal = kcalOf(value, unit)
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_NAME to itemName,
                RESULT_KCAL to kcal,
                RESULT_QUANTITY to "${formatNumber(value)} ${labelOf(unit)}",
                RESULT_DATETIME to selectedAt.format(TIME_FORMATTER)
            )
        )
        sportRepo.add(
            recordedAt = selectedAt,
            name = itemName,
            quantity = value,
            unitLabel = labelOf(unit),
            kcal = kcal
        )
        dismiss()
    }

    private fun formatNumber(value: Float): String =
        if (value % 1f == 0f) value.toInt().toString()
        else String.format(Locale.US, "%.1f", value)

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "SportInputSheet"
        const val REQUEST_KEY = "sport_input_request"
        const val RESULT_NAME = "sport_input_name"
        const val RESULT_KCAL = "sport_input_kcal"
        const val RESULT_QUANTITY = "sport_input_quantity"
        const val RESULT_DATETIME = "sport_input_time"

        private const val ARG_NAME = "arg_name"
        private const val ARG_KCAL = "arg_kcal"
        private const val ARG_UNIT = "arg_unit"
        private const val ARG_PER10 = "arg_per10"

        private const val MAX_LENGTH = 4

        /** 切单位时的默认数值：10 min / 5 km / 1 局 / 1 场 / 20 个 */
        private val DEFAULT_VALUE = mapOf(
            SportUnit.MIN to 10,
            SportUnit.KM to 5,
            SportUnit.ROUND to 1,
            SportUnit.SESSION to 1,
            SportUnit.REP to 20
        )

        private val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        fun newInstance(item: SportItem): SportInputSheet = SportInputSheet().apply {
            arguments = bundleOf(
                ARG_NAME to item.name,
                ARG_KCAL to item.kcalPer10Min,
                ARG_UNIT to item.secondUnit.name,
                ARG_PER10 to item.secondUnitsPer10Min
            )
        }
    }
}