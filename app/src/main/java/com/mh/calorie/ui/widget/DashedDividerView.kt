package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mh.calorie.R

/**
 * 虚线分隔线。
 *
 * 为什么不用 <shape android:shape="line">：
 *   1. line 形状沿 drawable 边界中线绘制，layout_height=1dp 时只剩半个像素，极易看不见；
 *   2. dashWidth/dashGap 依赖 DashPathEffect，在某些硬件加速路径下会被忽略，画出实线或干脆不画。
 * 这里直接按 dash/gap 循环画小矩形，不依赖 PathEffect，也不需要 layerType="software"。
 *
 * 线粗 = 控件高度（布局里给 1dp，那就是 1dp 的细线）。
 */
class DashedDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.divider)
    }

    private val dashWidth = 4f * density
    private val dashGap = 4f * density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 线粗跟随控件高度，太高太矮都自动适配
        val halfHeight = height / 2f
        val centerY = height / 2f

        var x = 0f
        while (x < width) {
            val end = (x + dashWidth).coerceAtMost(width.toFloat())
            canvas.drawRect(x, centerY - halfHeight, end, centerY + halfHeight, paint)
            x += dashWidth + dashGap
        }
    }
}