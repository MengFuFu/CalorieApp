package com.mh.calorie.ui.weight.record

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mh.calorie.R
import com.mh.calorie.core.domain.bmi.BmiCategory
import com.mh.calorie.databinding.FragmentWeightRecordBinding
import com.mh.calorie.ui.weight.input.WeightInputSheet
import com.mh.calorie.common.InfoDialogFragment
import com.mh.calorie.core.domain.model.BodyPart
import com.mh.calorie.databinding.ViewWeightBodyBinding
import com.mh.calorie.databinding.ViewWeightRecordWeightBinding
import com.mh.calorie.ui.weight.body.BodyInputSheet
import com.mh.calorie.ui.weight.body.BodyPartAdapter
import com.mh.calorie.ui.weight.body.BodyPartPickerPopup
import com.mh.calorie.ui.weight.body.BodyUiState
import com.mh.calorie.ui.weight.body.BodyViewModel
import com.mh.calorie.ui.weight.body.iconRes
import com.mh.calorie.ui.weight.body.labelRes
import com.mh.calorie.ui.widget.WeightChartView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class WeightRecordFragment : Fragment(R.layout.fragment_weight_record) {

    private var _binding: FragmentWeightRecordBinding? = null

    /** 外壳（btnBack / segType / pager） */
    private val rootBinding get() = _binding!!

    /** 体重页（pager 第 0 页）。仍叫 binding —— 下面几百行 binding.xxx 一行都不用改 */
    private lateinit var binding: ViewWeightRecordWeightBinding

    /** 围度页（pager 第 1 页） */
    private lateinit var bodyPage: ViewWeightBodyBinding
    // 必须和 WeightInputSheet 是同一个作用域，否则面板写的是另一个实例
    private val viewModel: WeightRecordViewModel by activityViewModels()

    private val bodyViewModel: BodyViewModel by activityViewModels()
    private lateinit var bodyAdapter: BodyPartAdapter

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeightRecordBinding.bind(view)

        // 两页先 inflate 好再交给 pager，避免翻页时反复 inflate 与状态丢失
        binding = ViewWeightRecordWeightBinding.inflate(layoutInflater, rootBinding.pager, false)
        bodyPage = ViewWeightBodyBinding.inflate(layoutInflater, rootBinding.pager, false)
        rootBinding.pager.adapter = RecordPagerAdapter(listOf(binding.root, bodyPage.root))
        // 只有两页，limit=1 就等于两页都常驻，不会回收
        rootBinding.pager.offscreenPageLimit = 1

        // 滑到哪页，分段控件就跟到哪
        rootBinding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                rootBinding.segType.select(position)
            }
        })

        // 围度网格
        bodyAdapter = BodyPartAdapter { part ->
            BodyInputSheet.newInstance(part).show(childFragmentManager, BodyInputSheet.TAG)
        }
        bodyPage.btnRecordBody.setOnClickListener {
            BodyInputSheet.newInstance().show(childFragmentManager, BodyInputSheet.TAG)
        }
        bodyPage.rvBodyParts.layoutManager = GridLayoutManager(requireContext(), 3)
        bodyPage.rvBodyParts.adapter = bodyAdapter

        // 历史围度：区间切换 + 部位筛选
        bodyPage.bodyChart.minSpan = 2f
        bodyPage.segBodyRange.setItems(
            listOf(getString(R.string.weight_range_month), getString(R.string.weight_range_week)), 0
        )
        bodyPage.segBodyRange.onSelected = { index ->
            bodyViewModel.setRange(if(index == 0) 30 else 7)
        }
        bodyPage.tvBodyPartFilter.setOnClickListener { showPartPicker() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bodyViewModel.uiState.collect { state ->
                    bodyAdapter.submit(state.rows)
                    renderBodyChart(state)
                }
            }
        }

        setupTopBar()
        setupSegments()
        observeState()
    }

    private fun setupTopBar() {
        rootBinding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnRecordWeight.setOnClickListener { showInputDialog() }
        binding.tvMore.setOnClickListener {
            findNavController().navigate(R.id.action_weightRecordFragment_to_weightHistoryFragment)
        }
        binding.ivEditStart.setOnClickListener {
            WeightInputSheet.Companion.newProfileInstance(
                WeightInputSheet.Field.START, viewModel.uiState.value.startWeightKg
            ).show(parentFragmentManager, WeightInputSheet.Companion.TAG)
        }
        binding.ivEditTarget.setOnClickListener {
            WeightInputSheet.Companion.newProfileInstance(
            WeightInputSheet.Field.TARGET, viewModel.uiState.value.targetWeightKg
        ).show(parentFragmentManager, WeightInputSheet.Companion.TAG) }
    }

    private fun setupSegments() {
        rootBinding.segType.setItems(
            listOf(getString(R.string.weight_tab_weight), getString(R.string.weight_tab_measure))
        )
        rootBinding.segType.onSelected = { index ->
            rootBinding.pager.setCurrentItem(index, true)   // 点 tab 也走滑动动画
        }
        rootBinding.segType.select(0)

        binding.segRange.setItems(
            listOf(getString(R.string.weight_range_month), getString(R.string.weight_range_week))
        )
        binding.segRange.onSelected = { index ->
            viewModel.selectRange(if (index == 0) 30 else 7)
        }

        parentFragmentManager.setFragmentResultListener(
            WeightInputSheet.Companion.REQUEST_KEY_PROFILE, viewLifecycleOwner
        ) { _, bundle ->
            val value = bundle.getFloat(WeightInputSheet.Companion.RESULT_VALUE)
            // 日期缺失（老版本面板/异常情况）就退回今天，保证不会崩
            when (bundle.getString(WeightInputSheet.Companion.RESULT_FIELD)) {
                WeightInputSheet.Field.START.name -> viewModel.setStartWeight(value)
                WeightInputSheet.Field.TARGET.name -> viewModel.setTargetWeight(value)
            }
        }

        binding.ivHelpBmi.setOnClickListener {
            InfoDialogFragment.newInstance(
                getString(R.string.bmi_help_title),
                getString(R.string.bmi_help_body),
                getString(R.string.bmi_help_formula)
            ).show(parentFragmentManager, InfoDialogFragment.TAG)
        }

        binding.ivHelpBfp.setOnClickListener {
            InfoDialogFragment.newInstance(
                getString(R.string.bfp_help_title),
                getString(R.string.bfp_help_body)
            ).show(parentFragmentManager, InfoDialogFragment.TAG)
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

        binding.tvStartValue.text = formatKg(state.startWeightKg)
        binding.tvTargetValue.text = formatKg(state.targetWeightKg)

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
        WeightInputSheet.Companion.newInstance(
            state.currentWeightKg,
            LocalDateTime.now(),
            weightByDate = state.weightByDate)
            .show(parentFragmentManager, WeightInputSheet.Companion.TAG)
    }

    /** 把状态铺到「历史围度」卡：部位胶囊 + 折线 + 空态 */
    private fun renderBodyChart(state: BodyUiState) {
        // 部位胶囊：文字 + 图标跟着当前选择走
        bodyPage.tvBodyPartFilter.setText(state.selectedPart.labelRes())
        bodyPage.tvBodyPartFilter.setCompoundDrawablesRelativeWithIntrinsicBounds(
            state.selectedPart.iconRes(), 0, R.drawable.ic_caret_down, 0
        )
        // 换过 drawable 后要把着色重新挂上，否则图标会变成原色
        TextViewCompat.setCompoundDrawableTintList(
            bodyPage.tvBodyPartFilter,
            ContextCompat.getColorStateList(requireContext(), R.color.text_secondary)
        )

        val points = state.chartPoints.map { WeightChartView.Point(it.date, it.label, it.valueCm) }
        bodyPage.bodyChart.valuePrefix = getString(state.selectedPart.labelRes())
        bodyPage.bodyChart.submit(points)

        val empty = points.isEmpty()
        bodyPage.bodyChart.isVisible = !empty
        bodyPage.tvBodyChartEmpty.isVisible = empty
    }

    /** 部位筛选：六个部位单选，选完立刻重算折线 */
    private fun showPartPicker() {
        BodyPartPickerPopup.show(
            anchor = bodyPage.tvBodyPartFilter,
            selected = bodyViewModel.uiState.value.selectedPart
        ) { part -> bodyViewModel.selectPart(part) }
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

    override fun onResume() {
        super.onResume()
        // 从历史页返回、从别的页面切回来时，按最新数据重绘一次
        _binding?.let { render(viewModel.uiState.value) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootBinding.pager.adapter = null
        _binding = null
    }
}