package com.mh.calorie.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentPlaceholderBinding

/**
 * 记录页（首页）占位。M6 会用真实首页布局整体替换掉这个类的内容。
 *
 * 这里不在字段上持有 binding，而是在 onViewCreated 里局部 bind：
 * 局部变量的生命周期不会超过视图，天然没有泄漏风险，也就不需要 _binding 那套判空。
 */
class HomeFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentPlaceholderBinding.bind(view).tvPlaceholderTitle.setText(R.string.page_title_home)
    }
}