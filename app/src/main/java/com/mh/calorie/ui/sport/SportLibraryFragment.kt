package com.mh.calorie.ui.sport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mh.calorie.R
import com.mh.calorie.databinding.FragmentSportLibraryBinding
import java.util.Locale

/** 「记运动」进来的运动库：左侧分类 + 右侧列表 + 底部合计，纯 UI，数据是静态的 */
class SportLibraryFragment : Fragment(R.layout.fragment_sport_library) {

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
        binding.tvCustomAdd.setOnClickListener { comingSoon(getString(R.string.sport_custom_add)) }
        binding.cardAi.setOnClickListener { comingSoon(getString(R.string.sport_ai_title)) }
        binding.btnDone.setOnClickListener {
            Toast.makeText(requireContext(), R.string.sport_done_coming, Toast.LENGTH_SHORT).show()
        }
        binding.btnGoAdd.setOnClickListener { comingSoon(getString(R.string.sport_custom_add)) }

        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = categoryAdapter

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = listAdapter

        binding.etSearch.doAfterTextChanged {
            keyword = it?.toString()?.trim().orEmpty()
            renderList()
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
        totalKcal += item.kcalPer10Min
        renderTotal()
        Toast.makeText(
            requireContext(),
            getString(R.string.sport_added_hint, item.name),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun renderList() {
        val all = SportCatalo.byCategory(current)
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