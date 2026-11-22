package com.mh.calorie.ui.stats

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.MealType
import com.mh.calorie.data.temp.TempFoodLogRepository
import com.mh.calorie.databinding.FragmentStatsBinding
import com.mh.calorie.databinding.ItemStatsFoodRowBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/** 统计页：上半天固定，饮食卡按餐次实时渲染 */
@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var foodRepo: TempFoodLogRepository

    private var date: LocalDate = LocalDate.now()
    private var meal: MealType = MealType.BREAKFAST

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentStatsBinding.bind(view)
        renderDate()
        renderMealTabs()

        binding.btnPrevDay.setOnClickListener {
            date = date.minusDays(1)
            renderDate()
            renderFood()
        }
        binding.btnNextDay.setOnClickListener {
            if (date.isBefore(LocalDate.now())) {
                date = date.plusDays(1)
                renderDate()
                renderFood()
            }
        }

        binding.cardReport.setOnClickListener { comingSoon(R.string.stats_report_title) }

        binding.btnAddFood.setOnClickListener {
            findNavController().navigate(R.id.action_statsFragment_to_foodLibraryFragment)
        }

        binding.btnAddSport.setOnClickListener { comingSoon(R.string.stats_sport_title) }
        binding.chipLifeAll.setOnClickListener { comingSoon(R.string.stats_life_title) }

        // 仓库一变就重绘（食物库保存后回到这页能立刻看到）
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                foodRepo.logs.collect { renderFood() }
            }
        }
    }

    private fun renderDate() {
        binding.tvDate.text = date.format(DATE_FORMAT)
    }

    /** 四个 tab：选中换绿底白字，没选中清背景变灰 */
    private fun renderMealTabs() {
        val tabs = listOf(
            MealType.BREAKFAST to binding.tvMealBreakfast,
            MealType.LUNCH to binding.tvMealLunch,
            MealType.DINNER to binding.tvMealDinner,
            MealType.SNACK to binding.tvMealSnack
        )
        tabs.forEach { (type, tab) ->
            val selected = type == meal
            tab.setBackgroundResource(if (selected) R.drawable.bg_stat_tab_selected else 0)
            tab.setTextColor(color(if (selected) R.color.white else R.color.text_secondary))
            tab.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
            tab.setOnClickListener {
                if (meal == type) return@setOnClickListener
                meal = type
                renderMealTabs()
                renderFood()
            }
        }
    }

    /** 上面的总量与三大宏量按「全天」统计；只有下面的食物卡片跟随餐次 tab */
    private fun renderFood() {
        // 全天合计：切换早/午/晚/加餐都不应该动
        val dayLogs = foodRepo.ofDate(date)
        val kcal = dayLogs.sumOf { it.kcal.toDouble() }.toFloat()
        val carb = dayLogs.sumOf { it.carbGram.toDouble() }.toFloat()
        val protein = dayLogs.sumOf { it.proteinGram.toDouble() }.toFloat()
        val fat = dayLogs.sumOf { it.fatGram.toDouble() }.toFloat()

        // 百分比用「三大宏量供能之和」做分母，三项加起来正好 100%
        val macroEnergy = carb * 4f + protein * 4f + fat * 9f
        fun share(part: Float): Int =
            if (macroEnergy <= 0f) 0
            else (part / macroEnergy * 100f).roundToInt().coerceIn(0, 100)

        val carbPercent = share(carb * 4f)
        val proteinPercent = share(protein * 4f)
        val fatPercent = share(fat * 9f)

        binding.tvIntakeKcal.text = String.format(Locale.US, "%dkcal", kcal.roundToInt())

        binding.tvCarbPercent.text = getString(R.string.stats_carb_percent, carbPercent)
        binding.tvProteinPercent.text = getString(R.string.stats_protein_percent, proteinPercent)
        binding.tvFatPercent.text = getString(R.string.stats_fat_percent, fatPercent)

        binding.pbCarb.progress = carbPercent
        binding.pbProtein.progress = proteinPercent
        binding.pbFat.progress = fatPercent

        binding.tvCarbGram.text = formatGram(carb)
        binding.tvProteinGram.text = formatGram(protein)
        binding.tvFatGram.text = formatGram(fat)

        // 食物卡片仍然只属于当前餐次
        val logs = foodRepo.ofMeal(date, meal)
        binding.containerFoods.removeAllViews()
        logs.forEach { log ->
            val row = ItemStatsFoodRowBinding.inflate(layoutInflater, binding.containerFoods, false)
            row.tvName.text = log.name
            row.tvKcal.text = log.kcal.roundToInt().toString()
            row.tvQty.text = log.quantityText
            row.tvCarb.text = formatGram(log.carbGram)
            row.tvProtein.text = formatGram(log.proteinGram)
            row.tvFat.text = formatGram(log.fatGram)
            binding.containerFoods.addView(row.root)
        }

        binding.containerFoods.isVisible = logs.isNotEmpty()
        binding.groupFoodEmpty.isVisible = logs.isEmpty()
    }
    /** 某宏量提供的热量占总热量的百分比，夹在 0..100 */
    private fun percentOf(partKcal: Float, totalKcal: Float): Int =
        if (totalKcal <= 0f) 0 else (partKcal / totalKcal * 100f).roundToInt().coerceIn(0, 100)

    private fun formatGram(v: Float): String =
        String.format(Locale.US, "%.2fg", v)

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)

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