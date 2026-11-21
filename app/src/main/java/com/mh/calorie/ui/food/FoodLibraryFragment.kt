package com.mh.calorie.ui.food

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.FoodLog
import com.mh.calorie.core.domain.model.MealType
import com.mh.calorie.data.temp.TempFoodLogRepository
import com.mh.calorie.databinding.FragmentFoodLibraryBinding
import com.mh.calorie.ui.common.labelRes
import com.mh.calorie.ui.common.next
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

/** 食物库：左侧分类 + 右侧列表 + 搜索。数据来自 FoodCatalog，M4 后换成 Room */
@AndroidEntryPoint
class FoodLibraryFragment : Fragment(R.layout.fragment_food_library) {

    @Inject
    lateinit var foodRepo: TempFoodLogRepository

    /** 底部条当前选中的餐次，点一下往后切 */
    private var meal: MealType = MealType.BREAKFAST

    /** 打开面板时记住分类，算宏量要用 */
    private var pendingCategory: FoodCategory? = null

    private var _binding: FragmentFoodLibraryBinding? = null
    private val binding get() = _binding!!

    private var category: FoodCategory = FoodCategory.STAPLE
    private var query: String = ""

    private val foodAdapter = FoodItemAdapter(unit = { category.unit }) { item ->
        pendingCategory = category
        FoodPortionSheet.newInstance(
            item = item,
            mode = category.portionMode,
            date = LocalDate.now().toString()
        ).show(childFragmentManager, FoodPortionSheet.TAG)
    }

    private val categoryAdapter = FoodCategoryAdapter { picked ->
        category = picked
        renderList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentFoodLibraryBinding.bind(view)

        binding.tvDate.text = LocalDate.now().format(DATE_FORMAT)

        // 底部条避开系统导航条
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

        binding.tvMeal.text = getString(meal.labelRes)
        binding.tvMeal.setOnClickListener {
            meal = meal.next()
            binding.tvMeal.text = getString(meal.labelRes)
        }

        childFragmentManager.setFragmentResultListener(
            FoodPortionSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString(FoodPortionSheet.RESULT_NAME)
                ?: return@setFragmentResultListener
            val kcal = bundle.getInt(FoodPortionSheet.RESULT_KCAL).toFloat()
            val qty = bundle.getString(FoodPortionSheet.RESULT_QTY).orEmpty()
            val unit = bundle.getString(FoodPortionSheet.RESULT_UNIT).orEmpty()

            val (c, p, f) = MacroEstimator.split(pendingCategory ?: category, kcal)
            foodRepo.add(
                FoodLog(
                    recordedAt = LocalDateTime.now(),
                    meal = meal,
                    name = name,
                    quantity = qty,
                    unit = unit,
                    kcal = kcal,
                    carbGram = c,
                    proteinGram = p,
                    fatGram = f
                )
            )

            makeText(
                requireContext(),
                getString(R.string.food_added_kcal_hint, name, kcal.roundToInt()),
                Toast.LENGTH_SHORT
            ).show()
        }

        renderList()
    }

    private fun renderList() {
        val source = FoodCatalog.itemsOf(category)
        val list = if(query.isEmpty()) source else source.filter { it.name.contains(query, ignoreCase = true) }
        foodAdapter.submitList(list)
        binding.tvEmpty.isVisible = list.isEmpty()
        binding.rvFood.isVisible = list.isNotEmpty()
    }

    private fun toast(resId: Int) {
        makeText(
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