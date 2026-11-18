package com.mh.calorie.ui.weight.body

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.BodyPart
import com.mh.calorie.databinding.SheetBodyInputBinding
import com.mh.calorie.databinding.ViewBodyInputRowBinding
import com.mh.calorie.ui.weight.input.DateWheelSheet
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 记录围度：六行输入 + 时间胶囊，保存后写仓库 */
@AndroidEntryPoint
class BodyInputSheet : BottomSheetDialogFragment() {

    private var _binding: SheetBodyInputBinding? = null
    private val binding get() = _binding!!

    // 和 WeightRecordFragment 用同一个 activity 作用域实例，保存后页面立刻收到新状态
    private val bodyViewModel: BodyViewModel by activityViewModels()

    private var selectedAt: LocalDateTime = LocalDateTime.now()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetBodyInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        renderRows()
        renderTime()

        // 时间胶囊 → 复用体重页那个日期滚轮
        binding.tvTime.setOnClickListener {
            DateWheelSheet.newInstance(selectedAt).show(childFragmentManager, DateWheelSheet.TAG)
        }
        childFragmentManager.setFragmentResultListener(
            DateWheelSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val text = bundle.getString(DateWheelSheet.RESULT_DATETIME)
                ?: return@setFragmentResultListener
            selectedAt = runCatching { LocalDateTime.parse(text, DateWheelSheet.FORMATTER) }
                .getOrElse { LocalDateTime.now() }
            renderTime()
        }

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { save() }

        // 从某一格的「+」进来时，直接聚焦那一行
        arguments?.getString(ARG_FOCUS)?.let { name ->
            runCatching { BodyPart.valueOf(name) }.getOrNull()?.let { editOf(it).requestFocus() }
        }
    }

    override fun onStart() {
        super.onStart()
        // 白底上圆角 + 展开到内容高度
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(R.drawable.bg_sheet_white)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** 六行固定顺序，和网格一致 */
    private fun rows(): List<Pair<BodyPart, ViewBodyInputRowBinding>> = listOf(
        BodyPart.WAIST to binding.rowWaist,
        BodyPart.CHEST to binding.rowChest,
        BodyPart.HIP to binding.rowHip,
        BodyPart.THIGH to binding.rowThigh,
        BodyPart.CALF to binding.rowCalf,
        BodyPart.ARM to binding.rowArm
    )

    private fun editOf(part: BodyPart) =
        rows().first { it.first == part }.second.etRowValue

    private fun renderRows() {
        // 已经记录过的部位直接回填，方便微调
        val latest = bodyViewModel.uiState.value.rows.associate { it.part to it.valueCm }
        rows().forEach { (part, row) ->
            row.tvRowLabel.setText(part.labelRes())
            row.ivRowIcon.setImageResource(part.iconRes())
            latest[part]?.let { row.etRowValue.setText(format(it)) }
        }
    }

    private fun renderTime() {
        binding.tvTime.text = selectedAt.format(TIME_FORMATTER)
    }

    private fun save() {
        val values = linkedMapOf<BodyPart, Float>()
        var outOfRange = false

        rows().forEach { (part, row) ->
            val text = row.etRowValue.text.toString().trim()
            if (text.isEmpty()) return@forEach          // 留空 = 这一项不记录
            val value = text.toFloatOrNull()
            if (value == null || value < MIN_CM || value > MAX_CM) {
                outOfRange = true
                return@forEach
            }
            values[part] = value
        }

        if (outOfRange) { toast(R.string.body_range_hint); return }
        if (values.isEmpty()) { toast(R.string.body_input_empty); return }

        bodyViewModel.save(values, selectedAt)
        hideKeyboard()
        dismiss()
    }

    private fun format(value: Float): String =
        if (value % 1f == 0f) value.toInt().toString()
        else String.format(Locale.US, "%.1f", value)

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService<InputMethodManager>() ?: return
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "BodyInputSheet"

        private const val ARG_FOCUS = "arg_focus"
        private const val MIN_CM = 1f
        private const val MAX_CM = 300f
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        /** [focus] 传进来的部位会被自动聚焦，传 null 就是普通「记录围度」 */
        fun newInstance(focus: BodyPart? = null): BodyInputSheet = BodyInputSheet().apply {
            if (focus != null) arguments = bundleOf(ARG_FOCUS to focus.name)
        }
    }
}