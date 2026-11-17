package com.mh.calorie.ui.weight

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mh.calorie.R
import com.mh.calorie.databinding.DialogWeightInputBinding

/**
 * 记录体重弹窗。
 *
 * 结果走 Fragment Result API 而不是「构造函数传 lambda」：
 * 屏幕旋转后 DialogFragment 会被系统重建，lambda 会丢，回调就永远等不到了。
 */
class WeightInputDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogWeightInputBinding.inflate(layoutInflater)
        val current = arguments?.getFloat(ARG_CURRENT) ?: 0f
        if (current > 0f) binding.etWeight.setText(formatKg(current))

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.weight_dialog_title)
            .setView(binding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_save, null)
            .create()

        // 必须自己接管正按钮：默认的 setPositiveButton 点击后立即 dismiss，
        // 校验不通过时就没法阻止关闭
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = binding.etWeight.text?.toString()?.trim()?.toFloatOrNull()
                if (value == null || value !in MIN_KG..MAX_KG) {
                    binding.tilWeight.error = getString(R.string.weight_dialog_invalid)
                    return@setOnClickListener
                }
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY, bundleOf(RESULT_WEIGHT to value)
                )
                dismiss()
            }
        }
        return dialog
    }

    private fun formatKg(v: Float): String =
        if (v == v.toInt().toFloat()) "%.0f".format(v) else "%.1f".format(v)

    companion object {
        const val TAG = "WeightInputDialog"
        const val REQUEST_KEY = "weight_input_request"
        const val RESULT_WEIGHT = "weight_input_value"
        private const val ARG_CURRENT = "arg_current"
        private const val MIN_KG = 20f
        private const val MAX_KG = 300f

        fun newInstance(currentKg: Float): WeightInputDialog = WeightInputDialog().apply {
            arguments = bundleOf(ARG_CURRENT to currentKg)
        }
    }
}