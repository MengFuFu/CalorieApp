package com.mh.calorie.ui.sport

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.mh.calorie.core.domain.model.SportDraft
import com.mh.calorie.data.temp.TempCustomSportRepository
import com.mh.calorie.data.temp.TempSportDraftRepository
import com.mh.calorie.data.temp.TempSportRecordRepository
import com.mh.calorie.databinding.FragmentSportLibraryBinding
import com.mh.calorie.databinding.ItemSportDraftRowBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** 「记运动」进来的运动库：左侧分类 + 右侧列表 + 底部草稿合计 */
@AndroidEntryPoint
class SportLibraryFragment : Fragment(R.layout.fragment_sport_library) {

    @Inject
    lateinit var customRepo: TempCustomSportRepository

    /** 点「＋」只进这里 */
    @Inject
    lateinit var draftRepo: TempSportDraftRepository

    /** 点「完成」才写这里 */
    @Inject
    lateinit var recordRepo: TempSportRecordRepository

    private var customItems: List<CustomSport> = emptyList()
    private var _binding: FragmentSportLibraryBinding? = null
    private val binding get() = _binding!!

    private val categoryAdapter = SportCategoryAdapter { category -> selectCategory(category) }
    private val listAdapter = SportLibraryAdapter { item -> onAdd(item) }

    private var current: SportCategory = SportCategory.FAVORITE
    private var keyword: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSportLibraryBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.cardAi.setOnClickListener { comingSoon(getString(R.string.sport_ai_title)) }
        binding.btnDone.setOnClickListener { finish() }
        binding.tvCustomAdd.setOnClickListener { openCustomAdd() }
        binding.btnGoAdd.setOnClickListener { openCustomAdd() }
        binding.groupTotal.setOnClickListener { toggleDrafts() }

        // 自定义运动一有变化就重刷列表
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                customRepo.items.collect {
                    customItems = it
                    if (current == SportCategory.CUSTOM) renderList()
                }
            }
        }

        // 草稿一变：列表打勾 + 底部合计
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                draftRepo.drafts.collect { drafts ->
                    listAdapter.setDrafted(drafts.map { it.name }.toSet())
                    renderTotal(drafts)
                    renderDrafts(drafts)
                }
            }
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = categoryAdapter

        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExercises.adapter = listAdapter

        binding.groupTotal.setOnClickListener { toggleDrafts() }
        // 点面板外面收起
        binding.scrimDraftPanel.setOnClickListener { setDraftsOpen(false) }

        binding.etSearch.doAfterTextChanged {
            keyword = it?.toString()?.trim().orEmpty()
            renderList()
        }

        childFragmentManager.setFragmentResultListener(
            SportInputSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString(SportInputSheet.RESULT_NAME).orEmpty()
            val kcal = bundle.getFloat(SportInputSheet.RESULT_KCAL)
            val value = bundle.getFloat(SportInputSheet.RESULT_VALUE)
            val unitLabel = bundle.getString(SportInputSheet.RESULT_UNIT).orEmpty()
            val at = bundle.getString(SportInputSheet.RESULT_DATETIME)
                ?.let { runCatching { LocalDateTime.parse(it, DATE_TIME) }.getOrNull() }
                ?: LocalDateTime.now()
            // 先放草稿，等「完成」再落库
            draftRepo.add(name, value, unitLabel, kcal, at)
        }

        renderList()
        renderTotal(draftRepo.drafts.value)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.groupDraftPanel.isVisible) {
                        setDraftsOpen(false)
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    /** 完成：把草稿批量写进记录，再退出页面 */
    private fun finish() {
        val drafts = draftRepo.drafts.value
        if (drafts.isEmpty()) {
            findNavController().navigateUp()
            return
        }
        drafts.forEach {
            recordRepo.add(
                recordedAt = it.recordedAt,
                name = it.name,
                quantity = it.quantity,
                unitLabel = it.unitLabel,
                kcal = it.kcal
            )
        }
        draftRepo.clear()
        setDraftsOpen(false)
        Toast.makeText(
            requireContext(),
            getString(R.string.sport_recorded_hint, drafts.size),
            Toast.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }

    /** 面板 + 遮罩一起开关，保证两者永远同步 */
    private fun setDraftsOpen(open: Boolean) {
        binding.groupDraftPanel.isVisible = open
        binding.scrimDraftPanel.isVisible = open
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
        val list = if (keyword.isEmpty()) all else all.filter { it.name.contains(keyword) }
        listAdapter.submitList(list)

        // 空态：有搜索词时不算「分类为空」，直接显示无结果
        val empty = list.isEmpty()
        binding.groupEmpty.isVisible = empty
        binding.rvExercises.isVisible = !empty
        if (empty) {
            binding.tvEmpty.text = when {
                keyword.isNotEmpty() -> getString(R.string.sport_empty_search, keyword)
                current == SportCategory.CUSTOM -> getString(R.string.sport_empty_custom)
                else -> getString(R.string.sport_empty_favorite)
            }
            binding.btnGoAdd.isVisible = keyword.isEmpty() && current == SportCategory.CUSTOM
        }
    }

    /** 已添加列表：一条一行，右侧垃圾桶删除 */
    private fun renderDrafts(drafts: List<SportDraft>) {
        binding.containerDrafts.removeAllViews()
        binding.tvDraftEmpty.isVisible = drafts.isEmpty()
        binding.containerDrafts.isVisible = drafts.isNotEmpty()

        drafts.forEach { draft ->
            val row = ItemSportDraftRowBinding.inflate(
                layoutInflater, binding.containerDrafts, false
            )
            row.tvName.text = draft.name
            row.tvDetail.text = getString(
                R.string.sport_draft_detail,
                formatNumber(draft.quantity),
                draft.unitLabel,
                formatNumber(draft.kcal)
            )
            row.btnDelete.setOnClickListener {
                draftRepo.remove(draft.id)
                if (draftRepo.drafts.value.isEmpty()) setDraftsOpen(false)
            }
            binding.containerDrafts.addView(row.root)
        }
    }

    private fun formatNumber(value: Float): String =
        if (value % 1f == 0f) value.toInt().toString()
        else String.format(Locale.US, "%.1f", value)

    private fun renderTotal(drafts: List<SportDraft>) {
        val total = drafts.sumOf { it.kcal.toDouble() }.toFloat()
        binding.tvTotalKcal.text = if (total % 1f == 0f) {
            total.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", total)
        }
    }

    private fun toggleDrafts() {
        setDraftsOpen(!binding.groupDraftPanel.isVisible)
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

    private companion object {
        val DATE_TIME: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)
    }
}