package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mh.calorie.R
import kotlinx.coroutines.selects.select

/**
 * 分段选择器（体重/围度、近一月/近一周 都用它）。
 *
 * 不用 RadioGroup：RadioGroup 的按钮自身带 Material 的内边距和涟漪，
 * 想做出「白色胶囊嵌在浅灰轨道里」还得一路覆盖样式，不如直接摆 TextView。
 */
class SegmentedControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    private val tabs = mutableListOf<TextView>()

    /** 只在用户点击时回调，程序调用 select() 不触发，避免切范围时回调套回调 */
    var onSelected: ((Int) -> Unit)? = null

    private val pad = (3f * resources.displayMetrics.density).toInt()

    init {
        orientation = HORIZONTAL
        background = ContextCompat.getDrawable(context, R.drawable.bg_segment_track)
        setPadding(pad, pad, pad, pad)
    }

    fun setItems(labels: List<String>, selectedIndex: Int = 0) {
        removeAllViews()
        tabs.clear()

        labels.forEachIndexed { index, label ->
            val tv = TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(ContextCompat.getColorStateList(context, R.color.segment_text))
                isClickable = true
                isFocusable = true
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener {
                    select(index)
                    onSelected?.invoke(index)
                }
            }
            tabs += tv
            addView(tv)
        }
        select(selectedIndex)
    }

    fun select(index: Int) {
        if(index !in tabs.indices) return
        tabs.forEachIndexed { i, tv ->
            val active = i == index
            tv.isSelected = active
            tv.setBackgroundResource(if (active) R.drawable.bg_segment_selected else 0)
            tv.setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
            tv.elevation = if (active) 1f * resources.displayMetrics.density else 0f
        }
    }

}