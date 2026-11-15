package com.mh.calorie.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentHomeBinding
import com.mh.calorie.databinding.FragmentPlaceholderBinding

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

        // 演示数据下的点击一律先给提示，避免用户以为坏了
        binding.cardSummary.setOnClickListener { comingSoon("热量详情", "M5") }
        binding.tvAiChatHint.setOnClickListener { comingSoon("AI 聊天", "M12") }
        binding.btnAiPhoto.setOnClickListener { comingSoon("AI 拍照识别", "M12") }
        binding.cardCanIEat.setOnClickListener { comingSoon("我可以吃吗", "M13") }
        binding.cardFoodPk.setOnClickListener { comingSoon("食物 PK", "M13") }
        binding.cardWeight.setOnClickListener { comingSoon("体重详情", "M8") }
        binding.btnRecordWeight.setOnClickListener { comingSoon("记体重", "M8") }
        binding.cardSport.setOnClickListener { comingSoon("运动详情", "M8") }
        binding.btnRecordSport.setOnClickListener { comingSoon("记运动", "M8") }
        binding.cardFasting.setOnClickListener { comingSoon("断食计划", "M9") }
        binding.btnRecordFasting.setOnClickListener { comingSoon("记断食", "M10") }
        binding.cardWater.setOnClickListener { comingSoon("喝水详情", "M6") }
        binding.btnRecordWater.setOnClickListener { comingSoon("记喝水", "M6") }
        binding.tvWaterOther.setOnClickListener { comingSoon("喝其他", "M6") }
    }

    private fun comingSoon(label: String, module: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, label, module),
            Toast.LENGTH_SHORT
        ).show()
    }
}