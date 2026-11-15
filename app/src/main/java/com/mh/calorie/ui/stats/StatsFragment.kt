package com.mh.calorie.ui.stats

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentPlaceholderBinding

/** 统计页占位（M8 实现：日报 / 体重曲线 / 营养饼图 / 日历回溯） */
class StatsFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentPlaceholderBinding.bind(view).tvPlaceholderTitle.setText(R.string.page_title_stats)
    }
}