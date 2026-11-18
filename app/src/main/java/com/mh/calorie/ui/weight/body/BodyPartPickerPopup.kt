package com.mh.calorie.ui.weight.body

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.BodyPart

/**
 * 部位下拉：贴着「腰围 ▾」胶囊下方弹出，六行「图标 + 名称」，选中项右侧打勾。
 * 用 PopupWindow 而不是 AlertDialog —— 设计稿是下拉，不是居中弹窗。
 */
object BodyPartPickerPopup {

    fun show(anchor: View, selected: BodyPart, onPick: (BodyPart) -> Unit) {
        val context = anchor.context
        val density = context.resources.displayMetrics.density
        fun dp(v: Float) = (v * density).toInt()

        val content = LayoutInflater.from(context)
            .inflate(R.layout.view_body_part_picker, null, false) as LinearLayout

        var popup: PopupWindow? = null

        BodyPart.entries.forEach { part ->
            val row = LayoutInflater.from(context)
                .inflate(R.layout.item_body_part_picker_row, content, false)

            row.findViewById<ImageView>(R.id.ivPartIcon).setImageResource(part.iconRes())
            row.findViewById<TextView>(R.id.tvPartName).setText(part.labelRes())
            row.findViewById<ImageView>(R.id.ivPartCheck).isVisible = part == selected

            row.setOnClickListener {
                popup?.dismiss()
                onPick(part)
            }
            content.addView(row)
        }

        // 宽度取「胶囊宽度」与 132dp 的较大者，太窄会显得局促
        val width = maxOf(anchor.width, dp(132f))
        popup = PopupWindow(content, width, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_picker_card))
            elevation = dp(8f).toFloat()   // 有背景 drawable 才有正常阴影
            isOutsideTouchable = true
        }
        popup.showAsDropDown(anchor, 0, dp(6f))
    }
}