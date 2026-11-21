package com.mh.calorie.ui.stats

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentPlaceholderBinding
import com.mh.calorie.databinding.FragmentStatsBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 统计页：目前只做静态 UI，数据等接真实数据源后再填 */
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    /** 当前查看的日期，只允许往前翻 */
    private var date: LocalDate = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentStatsBinding.bind(view)
        renderDate()

        binding.btnPrevDay.setOnClickListener {
            date = date.minusDays(1)
            renderDate()
        }
        binding.btnNextDay.setOnClickListener {
            if (date.isBefore(LocalDate.now())) {
                date = date.plusDays(1)
                renderDate()
            }
        }

        binding.cardReport.setOnClickListener { comingSoon(R.string.stats_report_title) }
        binding.btnAddFood.setOnClickListener { comingSoon(R.string.stats_diet_title) }
        binding.btnAddSport.setOnClickListener { comingSoon(R.string.stats_sport_title) }
        binding.chipLifeAll.setOnClickListener { comingSoon(R.string.stats_life_title) }
    }

    private fun renderDate() {
        binding.tvDate.text = date.format(DATE_FORMAT)
    }

    private fun comingSoon(titleRes: Int) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, getString(titleRes), getString(R.string.page_title_stats)),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA)
    }
}