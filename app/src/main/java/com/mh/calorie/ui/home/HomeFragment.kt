package com.mh.calorie.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentHomeBinding
import androidx.core.view.updatePadding
import androidx.navigation.fragment.findNavController

/**
 * 记录页（首页）占位。M6 会用真实首页布局整体替换掉这个类的内容。
 *
 * 这里不在字段上持有 binding，而是在 onViewCreated 里局部 bind：
 * 局部变量的生命周期不会超过视图，天然没有泄漏风险，也就不需要 _binding 那套判空。
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        // 当前摄入 0，所以仪表进度是 0：只画出浅色轨道
        binding.arcGauge.setProgress(0f)

        // 弧端位置由 r 决定，只能在测量后才知道，所以走回调 + post
        binding.arcGauge.onArcMeasured = { endDx, _ ->
            binding.arcGauge.post { alignLabelsToArc(binding, endDx) }
        }

        // 演示数据下的点击一律先给提示，避免用户以为坏了
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
        // 运动卡 → 运动记录页；记运动 → 直接进运动库
        binding.cardSport.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sportRecordFragment)
        }
        binding.btnRecordSport.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sportLibraryFragment)
        }
        binding.cardFasting.setOnClickListener { comingSoon("断食计划", "M9") }
        binding.btnRecordFasting.setOnClickListener { comingSoon("记断食", "M10") }
        binding.cardWater.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_waterFragment)
        }
        binding.btnRecordWater.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_waterFragment)
        }
        binding.tvWaterOther.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_waterFragment)
        }
    }

    private fun comingSoon(label: String, module: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, label, module),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * 把左右两列文字推到弧的两个端点正下方。
     * 两列是 weight=1 均分的，无法靠 gravity 表达“居中在弧端”，
     * 只能换成内边距：左列的 paddingStart / 右列的 paddingEnd。
     */
    private fun alignLabelsToArc(binding: FragmentHomeBinding, endDx: Float) {
        val labelL = binding.tvIntakeLabel
        val labelR = binding.tvBurnedLabel
        if (labelL.width == 0 || labelR.width == 0) return

        val cx = binding.arcGauge.width / 2f
        val rowW = binding.llIntake.width + binding.llBurned.width

        // 左列内容左对齐：左边缘 = 弧端 x - 文字宽/2
        val padStart = (cx - endDx - labelL.width / 2f).toInt()
        binding.llIntake.updatePadding(left = padStart.coerceAtLeast(0))

        // 右列内容右对齐：右边缘 = 弧端 x + 文字宽/2
        val padEnd = (rowW - (cx + endDx) - labelR.width / 2f).toInt()
        binding.llBurned.updatePadding(right = padEnd.coerceAtLeast(0))
    }
}