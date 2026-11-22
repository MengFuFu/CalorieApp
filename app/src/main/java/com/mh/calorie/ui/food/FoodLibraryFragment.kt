package com.mh.calorie.ui.food

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.MealType
import com.mh.calorie.data.temp.TempFoodLogRepository
import com.mh.calorie.databinding.FragmentFoodLibraryBinding
import com.mh.calorie.databinding.ItemMealRecordRowBinding
import com.mh.calorie.ui.common.labelRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * 食物库：左侧分类 + 右侧列表。
 * 底部餐次 chip 可展开餐次面板，切换的餐次就是「往哪一餐里加」。
 */
@AndroidEntryPoint
class FoodLibraryFragment : Fragment(R.layout.fragment_food_library) {

    @Inject
    lateinit var foodRepo: TempFoodLogRepository

    private var _binding: FragmentFoodLibraryBinding? = null
    private val binding get() = _binding!!

    private var category: FoodCategory = FoodCategory.STAPLE
    private var query: String = ""

    /** 当前餐次：底部 chip、餐次面板、新加记录都用它 */
    private var meal: MealType = MealType.BREAKFAST

    private val date: LocalDate get() = LocalDate.now()

    private val foodAdapter = FoodItemAdapter(
        unit = { category.unit },
        countOf = { item -> foodRepo.countOf(date, meal, item.name) },
        onAdd = { item -> openPortion(item) },
        onPlus = { item -> foodRepo.idOf(date, meal, item.name)?.let { foodRepo.increase(it) } },
        onMinus = { item -> foodRepo.idOf(date, meal, item.name)?.let { foodRepo.decrease(it) } }
    )

    private val categoryAdapter = FoodCategoryAdapter { picked ->
        category = picked
        renderList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentFoodLibraryBinding.bind(view)

        binding.tvDate.text = date.format(DATE_FORMAT)

        // 底部条避开导航条
        ViewCompat.setOnApplyWindowInsetsListener(binding.groupBottom) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }

        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategory.adapter = categoryAdapter
        categoryAdapter.select(category)

        binding.rvFood.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFood.adapter = foodAdapter

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnCustomAdd.setOnClickListener { toast(R.string.food_custom_add) }
        binding.groupDate.setOnClickListener { toast(R.string.food_title) }
        binding.btnDone.setOnClickListener { findNavController().navigateUp() }

        binding.etSearch.doAfterTextChanged { text ->
            query = text?.toString()?.trim().orEmpty()
            renderList()
        }

        // 餐次 chip：展开 / 收起面板
        binding.tvMeal.setOnClickListener { setPanelOpen(!binding.groupMealPanel.isVisible) }

        // 点面板外的遮罩收起
        binding.scrimMealPanel.setOnClickListener { setPanelOpen(false) }

        // 返回键：面板开着就先收面板，否则正常退出页面
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.groupMealPanel.isVisible) {
                        setPanelOpen(false)
                    } else {
                        isEnabled = false   // 交还给系统，否则会拦掉后续返回
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        binding.tvPanelBreakfast.setOnClickListener { selectMeal(MealType.BREAKFAST) }
        binding.tvPanelLunch.setOnClickListener { selectMeal(MealType.LUNCH) }
        binding.tvPanelDinner.setOnClickListener { selectMeal(MealType.DINNER) }
        binding.tvPanelSnack.setOnClickListener { selectMeal(MealType.SNACK) }

        // 份量面板回传 → 落到当前餐次
        childFragmentManager.setFragmentResultListener(
            FoodPortionSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString(FoodPortionSheet.RESULT_NAME)
                ?: return@setFragmentResultListener
            val kcal = bundle.getInt(FoodPortionSheet.RESULT_KCAL).toFloat()
            val qtyText = bundle.getString(FoodPortionSheet.RESULT_QTY).orEmpty()
            val unit = bundle.getString(FoodPortionSheet.RESULT_UNIT).orEmpty()
            val qty = qtyText.toFloatOrNull() ?: 1f

            val (c, p, f) = MacroEstimator.split(category, kcal)
            foodRepo.addOrIncrease(date, meal, name, qty, unit, kcal, c, p, f)

            Toast.makeText(
                requireContext(),
                getString(R.string.food_added_kcal_hint, name, kcal.roundToInt()),
                Toast.LENGTH_SHORT
            ).show()
        }

        // 仓库一变：列表步进器、角标、面板记录一起刷新
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                foodRepo.logs.collect {
                    foodAdapter.notifyDataSetChanged()
                    renderMealChip()
                    renderBadge()
                    renderMealPanel()
                }
            }
        }

        renderList()
        renderMealChip()
        renderBadge()
    }

    private fun selectMeal(target: MealType) {
        if (meal == target) return
        meal = target
        renderMealChip()
        renderBadge()
        renderMealPanel()
        foodAdapter.notifyDataSetChanged()   // 步进器是「这个餐次的数量」，要重画
    }

    /** 面板与遮罩同开同关，避免出现「面板在、点外面没反应」 */
    private fun setPanelOpen(open: Boolean) {
        binding.groupMealPanel.isVisible = open
        binding.scrimMealPanel.isVisible = open
        if (open) renderMealPanel()
    }

    private fun renderMealChip() {
        binding.tvMeal.text = getString(meal.labelRes)
    }

    /** 角标 = 当前餐次一共几份 */
    private fun renderBadge() {
        val total = foodRepo.totalCount(date, meal)
        binding.tvBadge.isVisible = total > 0
        binding.tvBadge.text = if (total > 99) "99+" else total.toString()
    }

    /** 左侧四个餐次 + 右侧该餐次的记录 */
    private fun renderMealPanel() {
        val tabs = listOf(
            MealType.BREAKFAST to binding.tvPanelBreakfast,
            MealType.LUNCH to binding.tvPanelLunch,
            MealType.DINNER to binding.tvPanelDinner,
            MealType.SNACK to binding.tvPanelSnack
        )
        tabs.forEach { (type, tab) -> styleMealTab(tab, type == meal) }

        val logs = foodRepo.ofMeal(date, meal)
        val totalKcal = logs.sumOf { it.kcal.toDouble() }.roundToInt()
        binding.tvPanelTotal.text = getString(
            R.string.food_meal_records_total,
            logs.sumOf { it.count },
            totalKcal
        )

        binding.containerPanelRows.removeAllViews()
        logs.forEach { log ->
            val row = ItemMealRecordRowBinding.inflate(layoutInflater, binding.containerPanelRows, false)
            row.tvName.text = log.name
            row.tvKcal.text = getString(
                R.string.food_meal_record_kcal,
                log.kcal.roundToInt(),
                com.mh.calorie.core.domain.model.FoodLog.format(log.baseQty) + log.unit
            )
            row.tvCount.text = log.count.toString()
            row.btnPlus.setOnClickListener { foodRepo.increase(log.id) }
            row.btnMinus.setOnClickListener { foodRepo.decrease(log.id) }
            binding.containerPanelRows.addView(row.root)
        }

        binding.scrollPanelRows.isVisible = logs.isNotEmpty()
        binding.groupPanelEmpty.isVisible = logs.isEmpty()
    }

    private fun styleMealTab(tab: TextView, selected: Boolean) {
        tab.setBackgroundResource(if (selected) R.drawable.bg_category_selected else 0)
        tab.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (selected) R.color.text_primary else R.color.text_secondary
            )
        )
        tab.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun openPortion(item: FoodItem) {
        FoodPortionSheet.newInstance(
            item = item,
            mode = category.portionMode,
            date = date.toString()
        ).show(childFragmentManager, FoodPortionSheet.TAG)
    }

    private fun renderList() {
        val source = FoodCatalog.itemsOf(category)
        val list = if (query.isEmpty()) source
        else source.filter { it.name.contains(query, ignoreCase = true) }
        foodAdapter.submitList(list)
        binding.tvEmpty.isVisible = list.isEmpty()
        binding.rvFood.isVisible = list.isNotEmpty()
    }

    private fun toast(resId: Int) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, getString(resId), getString(R.string.food_title)),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCategory.adapter = null
        binding.rvFood.adapter = null
        _binding = null
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
    }
}