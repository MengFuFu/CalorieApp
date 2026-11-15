package com.mh.calorie.ui.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentPlaceholderBinding

/** 计划页占位（M9 实现：饮食计划 / 断食计划 / 运动计划） */
class PlanFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentPlaceholderBinding.bind(view).tvPlaceholderTitle.setText(R.string.page_title_plan)
    }
}