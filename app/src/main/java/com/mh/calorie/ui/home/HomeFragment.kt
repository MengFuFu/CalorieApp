package com.mh.calorie.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.data.temp.TempFoodLogRepository
import com.mh.calorie.data.temp.TempSportRecordRepository
import com.mh.calorie.data.temp.TempTargetRepository
import com.mh.calorie.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    @Inject
    lateinit var foodRepo: TempFoodLogRepository
    @Inject
    lateinit var sportRepo: TempSportRecordRepository
    @Inject
    lateinit var targetRepo: TempTargetRepository

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // 弧端位置只在测量后才知道，走回调 + post
        binding.arcGauge.onArcMeasured = { endDx, _ ->
            binding.arcGauge.post { alignLabelsToArc(binding, endDx) }
        }

        bindClicks()
        render()
    }

    /** 从食物库/运动库返回时会重新可见，这里刷一次，保证数字是最新的 */
    override fun onResume() {
        super.onResume()
        if (_binding != null) render()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------------------------------------------------------- 渲染

    private fun render() {
        val today = LocalDate.now()

        // 当日食物：热量 + 三大宏量都在 FoodLog 里按份数算好了
        val logs = foodRepo.ofDate(today)
        val intake = logs.sumOf { it.kcal.toDouble() }.toFloat()
        val carb = logs.sumOf { it.carbGram.toDouble() }.toFloat()
        val protein = logs.sumOf { it.proteinGram.toDouble() }.toFloat()
        val fat = logs.sumOf { it.fatGram.toDouble() }.toFloat()

        // 当日运动消耗
        val burned = sportRepo.all()
            .filter { it.date == today }
            .sumOf { it.kcal.toDouble() }
            .toFloat()

        val target = targetRepo.targetCalorie
        // 还可以吃 = 目标 + 运动 − 已摄入
        val remain = target + burned - intake

        binding.tvRemainingKcal.text = remain.coerceAtLeast(0f).roundToInt().toString()
        binding.tvIntakeKcal.text = getString(R.string.home_kcal_value, intake.roundToInt())
        binding.tvBurnedKcal.text = getString(R.string.home_kcal_value, burned.roundToInt())

        // 圆弧进度：吃掉的占「目标 + 运动」的比例，超了由控件自己夹到 1
        val budget = target + burned
        binding.arcGauge.setProgress(if (budget <= 0f) 0f else intake / budget)

        // 三条宏量
        renderMacro(binding, carb, targetRepo.carbTargetG,
            binding.pbCarb::setProgress, binding.tvCarbValue::setText)
        renderMacro(binding, protein, targetRepo.proteinTargetG,
            binding.pbProtein::setProgress, binding.tvProteinValue::setText)
        renderMacro(binding, fat, targetRepo.fatTargetG,
            binding.pbFat::setProgress, binding.tvFatValue::setText)
    }

    /** 一条宏量：数值文案 + 进度条百分比 */
    private fun renderMacro(
        binding: FragmentHomeBinding,
        current: Float,
        target: Float,
        setProgress: (Int) -> Unit,
        setText: (CharSequence) -> Unit
    ) {
        setText(getString(R.string.home_macro_value, gram(current), gram(target)))
        val percent = if (target <= 0f) 0 else (current / target * 100f).roundToInt()
        setProgress(percent.coerceIn(0, 100))
    }

    /** 160.8、261.76 这种：整数不带小数点，小数最多两位且去掉尾随 0 */
    private fun gram(v: Float): String =
        if (v % 1f == 0f) v.toInt().toString()
        else String.format(Locale.US, "%.2f", v).trimEnd('0').trimEnd('.')

    // ---------------------------------------------------------------- 点击

    private fun bindClicks() {
        binding.cardSummary.setOnClickListener { comingSoon("热量详情", "M5") }
        binding.tvAiChatHint.setOnClickListener { comingSoon("AI 聊天", "M12") }
        binding.btnAiPhoto.setOnClickListener { comingSoon("AI 拍照识别", "M12") }
        binding.cardCanIEat.setOnClickListener { comingSoon("我可以吃吗", "M13") }
        binding.cardFoodPk.setOnClickListener { comingSoon("食物 PK", "M13") }

        val toWeight: () -> Unit = {
            findNavController().navigate(R.id.action_homeFragment_to_weightRecordFragment)
        }
        binding.cardWeight.setOnClickListener { toWeight() }
        binding.btnRecordWeight.setOnClickListener { toWeight() }

        binding.cardSport.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sportRecordFragment)
        }
        binding.btnRecordSport.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sportLibraryFragment)
        }

        binding.cardFasting.setOnClickListener { comingSoon("断食计划", "M9") }
        binding.btnRecordFasting.setOnClickListener { comingSoon("记断食", "M10") }

        val toWater: () -> Unit = {
            findNavController().navigate(R.id.action_homeFragment_to_waterFragment)
        }
        binding.cardWater.setOnClickListener { toWater() }
        binding.btnRecordWater.setOnClickListener { toWater() }
        binding.tvWaterOther.setOnClickListener { toWater() }
    }

    private fun comingSoon(label: String, module: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, label, module),
            Toast.LENGTH_SHORT
        ).show()
    }

    /** 把左右两列文字推到弧的两个端点正下方（逻辑不变） */
    private fun alignLabelsToArc(binding: FragmentHomeBinding, endDx: Float) {
        val labelL = binding.tvIntakeLabel
        val labelR = binding.tvBurnedLabel
        if (labelL.width == 0 || labelR.width == 0) return

        val cx = binding.arcGauge.width / 2f
        val rowW = binding.llIntake.width + binding.llBurned.width

        val padStart = (cx - endDx - labelL.width / 2f).toInt()
        binding.llIntake.updatePadding(left = padStart.coerceAtLeast(0))

        val padEnd = (rowW - (cx + endDx) - labelR.width / 2f).toInt()
        binding.llBurned.updatePadding(right = padEnd.coerceAtLeast(0))
    }
}