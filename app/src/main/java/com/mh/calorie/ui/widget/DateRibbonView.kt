package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mh.calorie.R

/**
 * 日期缎带。
 *
 * 设计稿逐像素测出的形状（缎带本体 44dp 高）：
 *   左上是一条长 64dp 的斜尾（斜边约 34°）；
 *   右上 22.5dp 圆角，正好压在卡片 3dp 描边的中线上，拐角不留白缝；
 *   右下 16dp 的凹弧，缎带底边顺着它落进卡片右边框。
 *
 * 控件比缎带本体高 16dp，多出来的那块只画凹弧，其余透明。
 */
class DateRibbonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : TextView(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.brand_green)
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w > 0f && h > 0f) {
            val fillet = 16f * density                  // 落进边框的过渡弧高度
            val bandH = (h - fillet).coerceAtLeast(0f)  // 缎带本体高度
            val tail = 64f * density                    // 斜尾横向长度
            val rTop = 22.5f * density                  // 右上角，对齐描边内边缘

            path.reset()
            path.moveTo(0f, 0f)                       // 尾尖
            path.lineTo(w - rTop, 0f)                 // 上边缘
            path.quadTo(w, 0f, w, rTop)               // 右上圆角
            path.lineTo(w, h)                         // 右边缘下到过渡弧底部
            path.quadTo(w, bandH, w - fillet, bandH)  // 凹弧：绿色顺着弧度进边框
            path.lineTo(tail, bandH)                  // 底边
            path.close()                              // 斜边回到尾尖
            canvas.drawPath(path, fillPaint)
        }
        super.onDraw(canvas) // 文字照常由 TextView 绘制
    }
}