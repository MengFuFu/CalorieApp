package com.mh.calorie.ui.weight.record

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.mh.calorie.R
import com.mh.calorie.common.InfoDialogFragment
import com.mh.calorie.core.domain.bmi.BmiCalculator.category
import com.mh.calorie.core.domain.bmi.BmiCategory
import com.mh.calorie.core.domain.model.BodyPart
import com.mh.calorie.databinding.FragmentWeightRecordBinding
import com.mh.calorie.databinding.ViewWeightBodyBinding
import com.mh.calorie.databinding.ViewWeightRecordWeightBinding
import com.mh.calorie.ui.weight.body.BodyInputSheet
import com.mh.calorie.ui.weight.body.BodyPartAdapter
import com.mh.calorie.ui.weight.body.BodyPartPickerPopup
import com.mh.calorie.ui.weight.body.BodyUiState
import com.mh.calorie.ui.weight.body.BodyViewModel
import com.mh.calorie.ui.weight.body.iconRes
import com.mh.calorie.ui.weight.body.labelRes
import com.mh.calorie.ui.weight.input.WeightInputSheet
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
        bodyPage.bodyChart.showGuide = false
        bodyPage.segBodyRange.setItems(
            listOf(getString(R.string.weight_range_month), getString(R.string.weight_range_week)), 0
        )
        bodyPage.segBodyRange.onSelected = { index ->
            bodyViewModel.setRange(if(index == 0) 30 else 7)
        }
        bodyPage.tvBodyPartFilter.setOnClickListener { showPartPicker() }
        // 历史围度 › 查看更多
        bodyPage.tvBodyMore.setOnClickListener {
            findNavController().navigate(R.id.action_weightRecordFragment_to_bodyHistoryFragment)
        }

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
            WeightInputSheet.newProfileInstance(
                WeightInputSheet.Field.START, viewModel.uiState.value.startWeightKg
            ).show(parentFragmentManager, WeightInputSheet.TAG)
        }
        binding.ivEditTarget.setOnClickListener {
            WeightInputSheet.newProfileInstance(
            WeightInputSheet.Field.TARGET, viewModel.uiState.value.targetWeightKg
        ).show(parentFragmentManager, WeightInputSheet.TAG) }
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
            WeightInputSheet.REQUEST_KEY_PROFILE, viewLifecycleOwner
        ) { _, bundle ->
            val value = bundle.getFloat(WeightInputSheet.RESULT_VALUE)
            // 日期缺失（老版本面板/异常情况）就退回今天，保证不会崩
            when (bundle.getString(WeightInputSheet.RESULT_FIELD)) {
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

        // 先算出类别，标签文字与底色都由它决定
        val category = category(state.bmi)
        binding.tvBmiTag.text = category.label
        binding.tvBmiTag.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), bmiSolidColor(category))
        binding.tvBmiTag.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

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

        track.post {
            val trackWidth = track.width
            if (trackWidth <= 0 || chip.width <= 0) return@post

            // 左右各留 4dp：阴影画在 View 外面，贴边一定会被裁
            val pad = 4f * resources.displayMetrics.density
            val minX = pad
            val maxX = (trackWidth - chip.width - pad).coerceAtLeast(minX)

            // 胶囊中心对准进度头，再夹进可视范围
            val headOffset = trackWidth * (percent.coerceIn(0, 100) / 100f)
            chip.translationX = (headOffset - chip.width / 2f).coerceIn(minX, maxX)
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
        WeightInputSheet.newInstance(
            state.currentWeightKg,
            LocalDateTime.now(),
            weightByDate = state.weightByDate)
            .show(parentFragmentManager, WeightInputSheet.TAG)
    }

    /** 把状态铺到「历史围度」卡：部位胶囊 + 折线 + 空态 */
    private fun renderBodyChart(state: BodyUiState) {
        applyPartChip(state.selectedPart)

        val points = state.chartPoints.map { WeightChartView.Point(it.date, it.label, it.valueCm) }
        bodyPage.bodyChart.valuePrefix = getString(state.selectedPart.labelRes())
        bodyPage.bodyChart.submit(points)

        val empty = points.isEmpty()
        bodyPage.bodyChart.isVisible = !empty
        bodyPage.tvBodyChartEmpty.isVisible = empty
    }

    /** 部位胶囊：图标 16dp、三角 12dp。必须手动 setBounds ——
     *  compound drawable 默认按矢量 intrinsic 尺寸布局，那五张图是 130~190dp，会把胶囊撑爆 */
    private fun applyPartChip(part: BodyPart) {
        val density = resources.displayMetrics.density
        val icon = ContextCompat.getDrawable(requireContext(), part.iconRes())?.mutate()
        val caret = ContextCompat.getDrawable(requireContext(), R.drawable.ic_caret_down)?.mutate()
        icon?.setBounds(0, 0, (16 * density).toInt(), (16 * density).toInt())
        caret?.setBounds(0, 0, (12 * density).toInt(), (12 * density).toInt())
        bodyPage.tvBodyPartFilter.text = getString(part.labelRes())
        bodyPage.tvBodyPartFilter.setCompoundDrawables(icon, null, caret, null)
    }

    /** 部位筛选：六个部位单选，选完立刻重算折线 */
    private fun showPartPicker() {
        BodyPartPickerPopup.show(
            anchor = bodyPage.tvBodyPartFilter,
            selected = bodyViewModel.uiState.value.selectedPart
        ) { part -> bodyViewModel.selectPart(part) }
    }

    /** 标签底色：四段色卡的同款实色，配白字 */
    private fun bmiSolidColor(category: BmiCategory): Int = when (category) {
        BmiCategory.LOW -> R.color.bmi_low
        BmiCategory.NORMAL -> R.color.bmi_normal
        BmiCategory.OVERWEIGHT -> R.color.bmi_over
        BmiCategory.OBESE -> R.color.bmi_obese
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