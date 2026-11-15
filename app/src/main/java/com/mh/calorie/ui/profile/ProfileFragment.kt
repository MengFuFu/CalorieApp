package com.mh.calorie.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentPlaceholderBinding

/** 我的页占位（M14 实现：档案 / 会员 / 特色工具入口） */
class ProfileFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentPlaceholderBinding.bind(view).tvPlaceholderTitle.setText(R.string.page_title_profile)
    }
}