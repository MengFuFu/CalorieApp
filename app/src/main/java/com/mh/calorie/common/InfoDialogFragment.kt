package com.mh.calorie.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.mh.calorie.R
import com.mh.calorie.databinding.DialogInfoTextBinding

/**
 * 通用说明弹窗：标题 + 正文 +（可选）公式框 + 我知道了。
 * 文案直接传字符串，Fragment 重建后仍在 arguments 里。
 */
class InfoDialogFragment : DialogFragment() {

    private var _binding: DialogInfoTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 宽度、背景、标题栏全部交给主题，避免 setLayout 被 windowMinWidth 覆盖
        setStyle(STYLE_NO_TITLE, R.style.Theme_Momo_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogInfoTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()

        binding.tvTitle.text = args.getString(ARG_TITLE).orEmpty()
        binding.tvBody.text = args.getString(ARG_BODY).orEmpty()

        val formula = args.getString(ARG_FORMULA)
        binding.tvFormula.isVisible = !formula.isNullOrBlank()
        binding.tvFormula.text = formula

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnOk.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_BODY = "arg_body"
        private const val ARG_FORMULA = "arg_formula"

        const val TAG = "InfoDialogFragment"

        /** formula 传 null 就是没有公式框（体脂率） */
        fun newInstance(title: String, body: String, formula: String? = null): InfoDialogFragment =
            InfoDialogFragment().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_BODY to body,
                    ARG_FORMULA to formula
                )
            }
    }
}