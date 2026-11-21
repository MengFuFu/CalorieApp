package com.mh.calorie.ui.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mh.calorie.R
import com.mh.calorie.databinding.SheetFoodPortionBinding
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 份量面板：输入克数/毫升/份数，或切到第二单位（个/根/杯…）输入个数，实时算热量。
 */
class FoodPortionSheet : BottomSheetDialogFragment() {

    private var _binding: SheetFoodPortionBinding? = null
    private val binding get() = _binding!!

    private var mode = PortionMode.WEIGHT
    private var per100 = 0f          // 每 100g / 100ml / 每份 的热量
    private var portionUnit: String? = null
    private var gramPerUnit = 0f

    private var usePortionUnit = false
    private val input = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetFoodPortionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mode = runCatching { PortionMode.valueOf(arguments?.getString(ARG_MODE).orEmpty()) }
            .getOrDefault(PortionMode.WEIGHT)
        per100 = arguments?.getFloat(ARG_KCAL) ?: 0f
        portionUnit = arguments?.getString(ARG_UNIT)
        gramPerUnit = arguments?.getFloat(ARG_GRAMS) ?: 0f
        // 没配到克重就没法换算，第二单位直接不给选
        if (gramPerUnit <= 0f) portionUnit = null

        binding.tvTitle.text = arguments?.getString(ARG_NAME)?.let {
            getString(R.string.food_portion_title)
        } ?: getString(R.string.food_portion_title)

        input.append(mode.defaultValue)

        bindKeypad()
        binding.tvUnit1.setOnClickListener { switchUnit(false) }
        binding.tvUnit2.setOnClickListener { switchUnit(true) }
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { save() }

        render()
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

    /** 键盘没有 id，按文本绑定，避免往布局里塞 10 个 id */
    private fun bindKeypad() {
        val rows = listOf(
            binding.root.findViewWithTag<LinearLayout>(KEYPAD_TAG)
        )
        // 直接遍历三层 LinearLayout 找 TextView
        walk(binding.root).forEach { tv ->
            val text = tv.text?.toString().orEmpty()
            when {
                text == "⌫" -> tv.setOnClickListener { onDelete() }
                text == "." -> tv.setOnClickListener { onKey('.') }
                text.length == 1 && text[0].isDigit() -> tv.setOnClickListener { onKey(text[0]) }
            }
        }
    }

    private fun walk(view: View): List<TextView> = when (view) {
        is TextView -> listOf(view)
        is ViewGroup -> (0 until view.childCount).flatMap { walk(view.getChildAt(it)) }
        else -> emptyList()
    }

    private fun onKey(ch: Char) {
        if (ch == '.') {
            if (input.contains('.')) return
            if (input.isEmpty()) input.append('0')
            input.append('.')
        } else {
            if (input.length >= 5) return          // 最多 "9999" 或 "999.9"
            if (input.toString() == "0") input.clear()
            input.append(ch)
        }
        render()
    }

    private fun onDelete() {
        if (input.isNotEmpty()) input.deleteCharAt(input.length - 1)
        render()
    }

    private fun switchUnit(toPortion: Boolean) {
        if (toPortion && portionUnit == null) return
        if (usePortionUnit == toPortion) return
        usePortionUnit = toPortion
        // 换单位就把默认值重设，避免 1 个 → 1g 这种荒谬值
        input.clear()
        input.append(if (toPortion) "1" else mode.defaultValue)
        render()
    }

    private fun value(): Float = input.toString().toFloatOrNull() ?: 0f

    /** 热量换算：按份 → 每份热量×份数；按个 → 每100×克数/100；按克 → 每100×克数/100 */
    private fun kcalOf(v: Float): Float = when {
        mode == PortionMode.PORTION -> per100 * v
        usePortionUnit -> per100 * v * gramPerUnit / 100f
        else -> per100 * v / 100f
    }

    private fun render() {
        val v = value()
        binding.tvValue.text = input.ifEmpty { "0" }

        val selected = ContextCompat.getColor(requireContext(), R.color.brand_green)
        val normal = ContextCompat.getColor(requireContext(), R.color.text_tertiary)

        binding.tvUnit1.text = mode.baseUnit
        binding.tvUnit1.setTextColor(if (usePortionUnit) normal else selected)

        binding.tvUnit2.isVisible = portionUnit != null
        binding.tvUnit2.text = portionUnit.orEmpty()
        binding.tvUnit2.setTextColor(if (usePortionUnit) selected else normal)

        binding.tvGramHint.isVisible = usePortionUnit && gramPerUnit > 0f
        if (usePortionUnit) {
            binding.tvGramHint.text = getString(
                R.string.food_portion_approx,
                formatNumber(v * gramPerUnit),
                mode.baseUnit
            )
        }

        binding.tvKcal.text = getString(R.string.food_portion_kcal, kcalOf(v).roundToInt())
    }

    private fun save() {
        val v = value()
        if (v <= 0f) {
            Toast.makeText(requireContext(), R.string.food_portion_empty, Toast.LENGTH_SHORT).show()
            return
        }
        // 重量/毫升最多 5000，份数/个数最多 100
        val max = if (usePortionUnit || mode == PortionMode.PORTION) 100f else 5000f
        if (v > max) {
            Toast.makeText(requireContext(), R.string.food_portion_too_large, Toast.LENGTH_SHORT).show()
            return
        }
        val unitLabel = if (usePortionUnit) portionUnit.orEmpty() else mode.baseUnit
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_NAME to arguments?.getString(ARG_NAME),
                RESULT_KCAL to kcalOf(v).roundToInt(),
                RESULT_QTY to input.toString(),
                RESULT_UNIT to unitLabel,
                RESULT_DATE to arguments?.getString(ARG_DATE)
            )
        )
        dismiss()
    }

    private fun formatNumber(v: Float): String =
        if (v % 1f == 0f) v.toInt().toString()
        else String.format(Locale.US, "%.1f", v)

    companion object {
        const val TAG = "FoodPortionSheet"
        const val REQUEST_KEY = "food_portion_request"
        const val RESULT_NAME = "food_portion_name"
        const val RESULT_KCAL = "food_portion_kcal"
        const val RESULT_QTY = "food_portion_qty"
        const val RESULT_UNIT = "food_portion_unit"
        const val RESULT_DATE = "food_portion_date"

        private const val ARG_NAME = "arg_name"
        private const val ARG_KCAL = "arg_kcal"
        private const val ARG_MODE = "arg_mode"
        private const val ARG_UNIT = "arg_unit"
        private const val ARG_GRAMS = "arg_grams"
        private const val ARG_DATE = "arg_date"
        private const val KEYPAD_TAG = "keypad"

        fun newInstance(item: FoodItem, mode: PortionMode, date: String): FoodPortionSheet =
            FoodPortionSheet().apply {
                arguments = bundleOf(
                    ARG_NAME to item.name,
                    ARG_KCAL to item.kcal.toFloat(),
                    ARG_MODE to mode.name,
                    ARG_UNIT to item.portionUnit,
                    ARG_GRAMS to item.gramPerUnit,
                    ARG_DATE to date
                )
            }
    }
}