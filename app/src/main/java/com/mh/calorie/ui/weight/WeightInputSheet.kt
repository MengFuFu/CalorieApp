package com.mh.calorie.ui.weight

import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mh.calorie.R
import com.mh.calorie.databinding.SheetWeightInputBinding
import dagger.multibindings.LongKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 记录体重面板：底部滑出 + 自绘键盘。
 *
 * 为什么不用 EditText：
 * 1. 软键盘弹出会顶起整个弹窗，盖住大数字；
 * 2. 设计稿要求「大数字 + 绿色下划线 + 字段名」的排版，EditText 做不了；
 * 3. 自绘键盘可以把「一位小数、最多三位整数」这类规则直接写死，用户根本输不出非法值。
 *
 * 返回值仍走 Fragment Result API，key 与旧弹窗完全一致，调用方不用改。
 */
class WeightInputSheet : BottomSheetDialogFragment(){

    private var _binding: SheetWeightInputBinding? = null
    private val binding get() = _binding!!

    /** 用户已按下的字符（不含千分位等展示格式），渲染时再决定怎么显示 */
    private val input = StringBuilder()

    /** 校验失败后数字标红，下一次按键自动恢复 */
    private var hasError = false

    /** 记录时间；下一步接日期滚轮时只需要改这个字段 */
    private var selectedAt: LocalDateTime = LocalDateTime.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SheetWeightInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 先恢复时间，再渲染，否则第一次 render 会把胶囊重置成 now
        arguments?.getString(ARG_DATETIME)?.let {
            runCatching { LocalDateTime.parse(it, TIME_FORMATTER) }.getOrNull()
        }?.let { selectedAt = it }


        // 预填上一次的体重，用户改数字比从零开始快
        val current = arguments?.getFloat(ARG_CURRENT) ?: 0f
        if(current > 0f) input.append(formatKg(current))

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { save() }

        binding.tvTime.setOnClickListener {
            DateWheelSheet.newInstance(selectedAt).show(childFragmentManager, DateWheelSheet.TAG)
        }

        // 日期面板确认后回到这里，更新时间并重绘
        childFragmentManager.setFragmentResultListener(
            DateWheelSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(DateWheelSheet.RESULT_DATETIME)
            if(text != null) {
                selectedAt = LocalDateTime.parse(text, DateWheelSheet.FORMATTER)
                render()
            }
        }

        // 11 个字符键统一走一个入口，避免写 11 个几乎一样的 setOnClickListener
        listOf(
            binding.key0 to '0', binding.key1 to '1', binding.key2 to '2',
            binding.key3 to '3', binding.key4 to '4', binding.key5 to '5',
            binding.key6 to '6', binding.key7 to '7', binding.key8 to '8',
            binding.key9 to '9', binding.keyDot to '.'
        ).forEach { (key, char) -> key.setOnClickListener { onKey(char) } }

        binding.keyDel.setOnClickListener { onDelete() }

        render()
    }

    override fun onStart() {
        super.onStart()
        // design_bottom_sheet 默认带 Material 自己的直角白底，
        // 换成圆角 drawable 才是设计稿那种「上面两个圆角、下面贴底」的面板
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(R.drawable.bg_sheet_white)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    // ---------------- 键盘逻辑 ----------------

    private fun onKey(char: Char) {
        hasError = false

        if(char == '.') {
            when {
                input.isEmpty() -> input.append("0.") // 直接按小数点，补成 0.
                !input.contains(".") -> input.append(".") // 只允许一个小数点
            }
        } else {
            val dotIndex = input.indexOf(".")
            when {
                // 小数位最多一位：0.5 可以，0.55 不行
                dotIndex >= 0 && input.length - dotIndex > 1 -> Unit
                // 整数位最多三位：已经三位还想再加，提示一次
                dotIndex < 0 && input.length >= 3 -> showRangeHint()
                // 单独一个 0 被新数字顶掉，避免出现 "05"
                input.length == 1 && input[0] == '0' -> {
                    input.clear()
                    input.append(char)
                }
                else -> input.append(char)
            }
        }
        render()
    }

    private fun onDelete() {
        hasError = false
        if(input.isNotEmpty()) input.deleteCharAt(input.length - 1)
        render()
    }

    // ---------------- 渲染 ----------------
    private fun render() {
        binding.tvValue.text = if(input.isEmpty()) DEFAULT_TEXT else input.toString()
        binding.tvValue.setTextColor(
            ContextCompat.getColor(
            requireContext(),
            if (hasError) R.color.state_error else R.color.text_primary
            )
        )
        binding.tvTime.text = selectedAt.format(TIME_FORMATTER)
    }

    /** 上一次弹提示的时间，防止连按数字时排队弹一串 */
    private var lastHintAt = 0L

    /**
     * 把「半透明黑底」的提示压在体重数字上方。
     *
     * 系统默认 toast 在 Android 11+ 会忽略自定义背景与位置，
     * 所以这里自己 inflate 一个 TextView 当 toast.view，位置用 getLocationOnScreen 算。
     */
    private fun showRangeHint() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastHintAt < 1200) return   // 1.2 秒内只提示一次
        lastHintAt = now

        val toastView = layoutInflater.inflate(R.layout.view_toast_hint, null)
        toastView.findViewById<TextView>(R.id.tvToast).text = getString(R.string.weight_range_hint)

        // 先量一遍高度，才知道要往上偏多少
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        toastView.measure(spec, spec)

        // 取数字在屏幕上的位置，把 toast 放到它正上方
        val location = IntArray(2)
        binding.tvValue.getLocationOnScreen(location)
        val gap = resources.getDimensionPixelSize(R.dimen.space_8)

        Toast(requireContext()).apply {
            duration = Toast.LENGTH_SHORT
            view = toastView
            setGravity(
                Gravity.TOP,                                             // 只剩 TOP 时横向自动居中
                0,
                location[1] - toastView.measuredHeight - gap
            )
        }.show()
    }

    // ---------------- 保存 ----------------

    private fun save() {
        val value = input.toString().toFloatOrNull()
        if(value == null || value < MIN_KG || value > MAX_KG) {
            hasError = true
            render()
            return
        }
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_WEIGHT to value,
                RESULT_DATETIME to selectedAt.format(TIME_FORMATTER)
            )
        )
        dismiss()
    }

    /** 整数不显示小数位：52 而不是 52.0 */
    private fun formatKg(v: Float): String =
        if(v == v.toInt().toFloat()) String.format(Locale.US, "%.0f", v)
        else String.format(Locale.US, "%.1f", v)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "WeightInputSheet"

        /** 与旧弹窗保持一致的 key，调用方代码不用跟着改 */
        const val REQUEST_KEY = "weight_input_request"
        const val RESULT_WEIGHT = "weight_input_value"
        const val RESULT_DATETIME = "weight_input_time"

        private const val ARG_CURRENT = "arg_current"
        private const val MIN_KG = 20f
        private const val MAX_KG = 300f
        private const val DEFAULT_TEXT = "0.0"

        val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        private const val ARG_DATETIME = "arg_datetime"

        fun newInstance(currentKg: Float, at: LocalDateTime = LocalDateTime.now()): WeightInputSheet =
            WeightInputSheet().apply {
            arguments = bundleOf(
                ARG_CURRENT to currentKg,
                ARG_DATETIME to at.format(TIME_FORMATTER))
        }
    }
}