package com.mh.calorie.ui.weight

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.core.domain.bmi.BmiCategory
import com.mh.calorie.databinding.FragmentWeightRecordBinding
import com.mh.calorie.ui.widget.WeightChartView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class WeightRecordFragment : Fragment(R.layout.fragment_weight_record) {

    private var _binding: FragmentWeightRecordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WeightRecordViewModel by viewModels()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeightRecordBinding.bind(view)

        setupTopBar()
        setupSegments()
        observeState()
    }

    private fun setupTopBar() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnRecordWeight.setOnClickListener { showInputDialog() }
        binding.tvMore.setOnClickListener { toast(R.string.weight_more_coming) }
        binding.ivEditStart.setOnClickListener { toast(R.string.weight_edit_coming) }
        binding.ivEditTarget.setOnClickListener { toast(R.string.weight_edit_coming) }
    }

    private fun setupSegments() {
        binding.segType.setItems(
            listOf(getString(R.string.weight_tab_weight), getString(R.string.weight_tab_measure))
        )
        binding.segType.onSelected = { index ->
            // 围度还没做：提示后把选中态拨回「体重」，否则用户会以为界面坏了
            if (index == 1) {
                toast(R.string.weight_measure_coming)
                binding.segType.select(0)
            }
        }

        binding.segRange.setItems(
            listOf(getString(R.string.weight_range_month), getString(R.string.weight_range_week))
        )
        binding.segRange.onSelected = { index ->
            viewModel.selectRange(if (index == 0) 30 else 7)
        }

        parentFragmentManager.setFragmentResultListener(
            WeightInputSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val kg = bundle.getFloat(WeightInputSheet.RESULT_WEIGHT)
            // 日期缺失（老版本面板/异常情况）就退回今天，保证不会崩
            val at = bundle.getString(WeightInputSheet.RESULT_DATETIME)
                ?.let { runCatching { LocalDateTime.parse(it, WeightInputSheet.TIME_FORMATTER) }.getOrNull() }
                ?: LocalDateTime.now()
            viewModel.addWeight(kg, at)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: WeightUiState) {
        binding.tvWeight.text = formatKg(state.currentWeightKg)
        binding.tvDate.text = state.lastRecordDate?.format(dateFormat).orEmpty()

        binding.tvProgressLabel.text = getString(
            if (state.isGainGoal) R.string.weight_progress_gain else R.string.weight_progress_lose
        )
        renderChanged(state.changedKg)   // 替换原来的 text + setTextColor 两行

        binding.pbProgress.progress = state.progressPercent
        binding.tvPercent.text = getString(R.string.weight_percent, state.progressPercent)
        movePercentChip(state.progressPercent)

        binding.tvStartWeight.text = getString(R.string.weight_start, formatKg(state.startWeightKg))
        binding.tvTargetWeight.text = getString(R.string.weight_target, formatKg(state.targetWeightKg))

        binding.tvBmi.text = String.format(Locale.US, "%.2f", state.bmi)
        binding.tvBmiTag.text = state.bmiCategory.label
        binding.tvBmiTag.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), tagChipColor(state.bmiCategory)
        )
        binding.tvBmiTag.setTextColor(ContextCompat.getColor(requireContext(), tagTextColor(state.bmiCategory)))
        binding.bmiScale.bmi = state.bmi

        binding.tvBodyFat.text = state.bodyFatPercent
            ?.let { String.format(Locale.US, "%.1f%%", it) }
            ?: getString(R.string.weight_body_fat_empty)

        binding.chart.submit(
            state.chartPoints.map { WeightChartView.Point(it.date, it.label, it.weightKg) }
        )
    }

    /**
     * 百分比胶囊跟着进度头部跑：
     * 0% 时贴轨道左端，100% 时贴轨道右端（右边缘对齐，不越界）。
     *
     * 用 translationX 而不是改 LayoutParams，避免每次数据刷新都触发一次完整布局；
     * post{} 是因为首帧宽度还没测量完，硬算会得到 0 或者负数。
     */
    private fun movePercentChip(percent: Int) {
        val track = binding.pbProgress
        val chip = binding.tvPercent

        // 等这一帧的测量完成再算，width 才是真实像素值
        track.post {
            val trackWidth = track.width
            if (trackWidth <= 0 || chip.width <= 0) return@post   // 还没测量好，跳过

            // 可移动范围 = 轨道宽度 - 胶囊宽度，保证 100% 时胶囊右边缘正好压住轨道末端
            val maxOffset = (trackWidth - chip.width).coerceAtLeast(0).toFloat()

            // 进度头部位置（像素），再夹到合法区间
            val headOffset = trackWidth * (percent.coerceIn(0, 100) / 100f)

            chip.translationX = headOffset.coerceIn(0f, maxOffset)
        }
    }

    /**
     * 「累计变化 -3 公斤」里只有数值变色，前后的文字保持 TextView 自己的颜色。
     * 先用完整格式串拼出整句，再定位数值的起止下标上色，这样中英文语序变化也不会错位。
     */
    private fun renderChanged(changedKg: Float) {
        val value = formatSigned(changedKg)                       // "-3" / "+2" / "0"
        val full = getString(R.string.weight_changed, value)      // "累计变化 -3 公斤"
        val start = full.indexOf(value)

        binding.tvChanged.text = if (start < 0) {
            full                                                  // 兜底：定位失败就整句不变色
        } else {
            SpannableString(full).apply {
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(requireContext(), changedColor(changedKg))
                    ),
                    start,
                    start + value.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun showInputDialog() {
        val state = viewModel.uiState.value
        // 默认今天；历史日期由面板里的日期滚轮改
        WeightInputSheet.newInstance(state.currentWeightKg, LocalDateTime.now())
            .show(parentFragmentManager, WeightInputSheet.TAG)
    }

    private fun tagChipColor(category: BmiCategory): Int = when (category) {
        BmiCategory.LOW -> R.color.chip_blue
        BmiCategory.NORMAL -> R.color.chip_green
        BmiCategory.OVERWEIGHT -> R.color.chip_yellow
        BmiCategory.OBESE -> R.color.chip_red
    }

    private fun tagTextColor(category: BmiCategory): Int = when (category) {
        BmiCategory.LOW -> R.color.accent_blue
        BmiCategory.NORMAL -> R.color.brand_green_dark
        BmiCategory.OVERWEIGHT -> R.color.accent_yellow_text
        BmiCategory.OBESE -> R.color.state_error
    }

    /** 整数不显示小数位：52 而不是 52.0 */
    private fun formatKg(v: Float): String =
        if (v == v.toInt().toFloat()) String.format(Locale.US, "%.0f", v)
        else String.format(Locale.US, "%.1f", v)

    /** 正数补加号，负数保留自带负号，0 不带符号 */
    private fun formatSigned(v: Float): String = when {
        v > 0f -> "+" + formatKg(v)   // +3
        v < 0f -> formatKg(v)         // -3（%.0f 自带负号）
        else -> formatKg(0f)          // 0
    }

    /** 体重上升=红，下降=绿，持平=灰 */
    private fun changedColor(v: Float): Int = when {
        v > 0f -> R.color.state_error        // #FF5A5A
        v < 0f -> R.color.brand_green_dark   // #4FA82F
        else -> R.color.text_secondary
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}