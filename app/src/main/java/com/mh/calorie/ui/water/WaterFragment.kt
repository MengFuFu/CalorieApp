package com.mh.calorie.ui.water

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.data.temp.TempWaterRepository
import com.mh.calorie.databinding.FragmentWaterBinding
import com.mh.calorie.databinding.ItemWaterRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** 喝水页：数据全部来自 TempWaterRepository，日历与首页共用同一份 records */
@AndroidEntryPoint
class WaterFragment : Fragment(R.layout.fragment_water) {

    private var _binding: FragmentWaterBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repo: TempWaterRepository

    /** 当前查看的日期，默认今天 */
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentWaterBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSettings.setOnClickListener { comingSoon(R.string.water_settings) }
        binding.btnEditGoal.setOnClickListener { comingSoon(R.string.water_goal_edit) }
        binding.btnCustom.setOnClickListener { comingSoon(R.string.water_custom) }

        binding.btnCalendar.setOnClickListener {
            WaterCalendarSheet().show(childFragmentManager, WaterCalendarSheet.TAG)
        }

        // 日历选完日期 → 页面切到那天
        childFragmentManager.setFragmentResultListener(
            WaterCalendarSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val iso = bundle.getString(WaterCalendarSheet.RESULT_DATE) ?: return@setFragmentResultListener
            selectedDate = runCatching { LocalDate.parse(iso) }.getOrElse { LocalDate.now() }
            render(repo.all())
        }

        binding.btnDrink.setOnClickListener {
            repo.add(LocalDateTime.of(selectedDate, LocalTime.now()), DEFAULT_CUP)
        }

        // 仓库一变，页面和日历都跟着变
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.records.collect { render(it) }
            }
        }
    }

    private fun render(all: List<com.mh.calorie.core.domain.model.WaterRecord>) {
        val dayRecords = all.filter { it.date == selectedDate }.sortedByDescending { it.recordedAt }
        val totalMl = dayRecords.sumOf { it.ml }
        val netMl = dayRecords.sumOf { it.netMl }
        val goal = repo.goalMl
        val percent = if (goal > 0) (netMl * 100 / goal).coerceIn(0, 100) else 0

        binding.tvTitle.text = if (selectedDate == LocalDate.now()) {
            getString(R.string.water_title)
        } else {
            selectedDate.format(DATE_FORMAT)
        }
        binding.tvPercent.text = getString(R.string.water_percent_value, percent)
        binding.tvGoal.text = getString(R.string.water_goal_ml, goal)
        binding.tvTodayMl.text = getString(R.string.water_today_ml, totalMl)
        binding.pbWater.progress = percent
        binding.pbWater.post { moveMarker(percent) }

        renderRecords(dayRecords)
    }

    private fun renderRecords(dayRecords: List<com.mh.calorie.core.domain.model.WaterRecord>) {
        binding.containerRecords.removeAllViews()
        val empty = dayRecords.isEmpty()
        binding.tvEmpty.isVisible = empty
        binding.scrollRecords.isVisible = !empty
        binding.scrollRecords.scrollTo(0, 0)   // 换日期后回到顶部
        if (empty) return

        val inflater = LayoutInflater.from(requireContext())
        dayRecords.forEach { record ->
            val row = ItemWaterRecordBinding.inflate(inflater, binding.containerRecords, false)
            row.tvName.text = getString(R.string.water_record_name)
            row.tvTime.text = record.recordedAt.format(TIME_FORMAT)
            row.tvAmount.text = getString(R.string.water_amount_ml_compact, record.ml)
            row.tvDetail.text = getString(R.string.water_detail, record.ratio, record.ml)
            binding.containerRecords.addView(row.root)
        }
    }

    private fun moveMarker(percent: Int) {
        val track = binding.pbWater
        val marker = binding.ivMarker
        if (track.width == 0) {
            track.doOnLayout { moveMarker(percent) }
            return
        }
        val trackWidth = track.width
        val markerWidth = marker.width
        val head = trackWidth * percent / 100f
        marker.translationX = (head - markerWidth / 2f)
            .coerceIn(0f, (trackWidth - markerWidth).toFloat())
    }

    private fun comingSoon(titleRes: Int) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, getString(titleRes), getString(R.string.water_module_name)),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val DEFAULT_CUP = 200
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
    }
}