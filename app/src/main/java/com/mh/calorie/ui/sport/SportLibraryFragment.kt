package com.mh.calorie.ui.sport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.CustomSport
import com.mh.calorie.data.temp.TempCustomSportRepository
import com.mh.calorie.databinding.FragmentSportLibraryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/** 「记运动」进来的运动库：左侧分类 + 右侧列表 + 底部合计，纯 UI，数据是静态的 */
@AndroidEntryPoint
class SportLibraryFragment : Fragment(R.layout.fragment_sport_library) {

    @Inject
    lateinit var customRepo: TempCustomSportRepository

    private var customItems: List<CustomSport> = emptyList()
    private var _binding: FragmentSportLibraryBinding? = null
    private val binding get() = _binding!!

    private val categoryAdapter = SportCategoryAdapter {category -> selectCategory(category)}
    private val listAdapter = SportLibraryAdapter {item -> onAdd(item)}

    private var current: SportCategory = SportCategory.FAVORITE
    private var keyword: String = ""
    /** 每次点 ＋ 加 10 分钟的量，先做 UI 反馈 */
    private var totalKcal: Float = 0f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSportLibraryBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.cardAi.setOnClickListener { comingSoon(getString(R.string.sport_ai_title)) }
        binding.btnDone.setOnClickListener {
            Toast.makeText(requireContext(), R.string.sport_done_coming, Toast.LENGTH_SHORT).show()
        }
        binding.tvCustomAdd.setOnClickListener { openCustomAdd() }
        binding.btnGoAdd.setOnClickListener { openCustomAdd() }

        // 自定义运动一有变化就重刷列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                customRepo.items.collect {
                    customItems = it
                    if (current == SportCategory.CUSTOM) renderList()
                }
            }
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = categoryAdapter

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = listAdapter

        binding.etSearch.doAfterTextChanged {
            keyword = it?.toString()?.trim().orEmpty()
            renderList()
        }

        childFragmentManager.setFragmentResultListener(
            SportInputSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val kcal = bundle.getFloat(SportInputSheet.RESULT_KCAL)
            val name = bundle.getString(SportInputSheet.RESULT_NAME).orEmpty()
            val quantity = bundle.getString(SportInputSheet.RESULT_QUANTITY).orEmpty()
            totalKcal += kcal
            renderTotal()
            Toast.makeText(
                requireContext(),
                getString(R.string.sport_added_hint, name, quantity),
                Toast.LENGTH_SHORT
            ).show()
        }

        renderList()
        renderTotal()
    }

    private fun selectCategory(category: SportCategory) {
        current = category
        categoryAdapter.select(category)
        renderList()
    }

    private fun onAdd(item: SportItem) {
        SportInputSheet.newInstance(item)
            .show(childFragmentManager, SportInputSheet.TAG)
    }

    private fun renderList() {
        val all = if (current == SportCategory.CUSTOM) {
            customItems.map { it.toSportItem() }
        } else {
            SportCatalog.byCategory(current)
        }
        val list = if(keyword.isEmpty()) all else all.filter { it.name.contains(keyword) }
        listAdapter.submitList(list)

        // 空态：有搜索词时不算「分类为空」，直接显示无结果
        val empty = list.isEmpty()
        binding.groupEmpty.isVisible = empty
        binding.rvExercises.isVisible = !empty
        if(empty) {
            binding.tvEmpty.text = when {
                keyword.isNotEmpty() -> getString(R.string.sport_empty_search, keyword)
                current == SportCategory.CUSTOM -> getString(R.string.sport_empty_custom)
                else -> getString(R.string.sport_empty_favorite)
            }
            binding.btnGoAdd.isVisible = keyword.isEmpty() && current == SportCategory.CUSTOM
        }
    }

    private fun renderTotal() {
        binding.tvTotalKcal.text = if(totalKcal % 1f == 0f) {
            totalKcal.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", totalKcal)
        }
    }

    private fun openCustomAdd() {
        findNavController().navigate(R.id.action_sportLibraryFragment_to_customSportFragment)
    }

    /** 自定义运动 → 统一模型：把「200 kcal / 30 分钟」折算成 kcalPer10Min，展示基准仍是 30 分钟 */
    private fun CustomSport.toSportItem(): SportItem = SportItem(
        category = SportCategory.CUSTOM,
        name = name,
        kcalPer10Min = kcal * 10f / minutes,
        secondUnit = SportUnit.MIN,
        secondUnitsPer10Min = 0f,
        sampleMinutes = minutes
    )

    private fun comingSoon(name: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.toast_coming_soon, name, getString(R.string.sport_module_name)),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCategories.adapter = null
        binding.rvExercises.adapter = null
        _binding = null
    }
}