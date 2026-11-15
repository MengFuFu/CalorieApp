package com.mh.calorie.ui.quickadd

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mh.calorie.R
import com.mh.calorie.databinding.SheetQuickAddBinding


/**
 * 全局快速添加弹窗（M11 会补全真实表单）。
 *
 * M2 阶段它存在的意义是把「加号 → 弹窗 → 选一项 → 关闭」这条链路先跑通，
 * 免得等功能都写完才发现入口接不上。
 */
class QuickAddSheet : BottomSheetDialogFragment() {

    private var _binding: SheetQuickAddBinding? = null

    // 非空断言封装：只在 onCreateView~onDestroyView 之间用，和 _binding 的生命周期严格对齐
    private val binding get() = requireNotNull(_binding) {"QuickAddSheet 在视图销毁后仍被访问"}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SheetQuickAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.itemFood.setOnClickListener { comingSoon(R.string.quick_add_food, "M7") }
        binding.itemWeight.setOnClickListener { comingSoon(R.string.quick_add_weight, "M8") }
        binding.itemSport.setOnClickListener { comingSoon(R.string.quick_add_sport, "M8") }
        binding.itemWater.setOnClickListener { comingSoon(R.string.quick_add_water, "M6") }
    }

    override fun onStart() {
        super.onStart()
        // 把 BottomSheet 自带的白色容器背景设为透明，
        // 否则它会在我们的圆角背景外面再套一层直角白底，圆角就被盖住了。
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun comingSoon(@StringRes labelRes: Int, module: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, getString(labelRes), module),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 必须置空，否则 Fragment 被复用时持有旧视图，造成内存泄漏
    }

    companion object {
        const val TAG = "QuickAddSheet"
    }
}