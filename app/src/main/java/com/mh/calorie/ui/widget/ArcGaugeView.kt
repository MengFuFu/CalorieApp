package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mh.calorie.R

/**
 * 首页的弧形仪表（半环形进度条）。
 *
 * 为什么自绘：Vico 2 只支持 Compose，本项目不用 Compose，所以图表类控件一律 Canvas 手写。
 * 这个控件只画一条弧，逻辑简单，但有几个几何坑值得写清楚（见 onSizeChanged）。
 *
 * 设计稿参数：粗 22dp、从 160°（8 点钟方向）顺时针扫 220° 到 20°（4 点钟方向），两端圆头。
 */
class ArcGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeWidthPx = 22f * resources.displayMetrics.density

    private val startAngle = 160f
    private val sweepAngle = 220f

    // 弧两端相对圆心的垂直偏移比例：sin(20°) ≈ 0.342
    private val endOffsetRatio = 0.342f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = strokeWidthPx
        color = ContextCompat.getColor(context, R.color.brand_green_surface)
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = strokeWidthPx
        color = ContextCompat.getColor(context, R.color.brand_green)
    }

    private val arcRect = RectF()
    private var progress = 0f

    /** 0f~1f。超出范围会被夹紧，不会画出多余的弧 */
    fun setProgress(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (clamped == progress) return
        progress = clamped
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 圆心的 y 在控件底边之上：弧的两端正好落在控件底边，
        // 于是 160°~380° 这一段露出来的就是「上半圈」，和设计稿一致。
        // 半径由两件事约束：
        //   1) 高度：顶端不能超出控件（h - 1.342 * r >= 0）
        //   2) 宽度：两端横向展开 2 * cos(20°) * r = 1.88 * r，不能超出控件宽度
        val maxRadiusByHeight = (h - strokeWidthPx) / (1f + endOffsetRatio)
        val maxRadiusByWidth = (w - strokeWidthPx) / (2f * 0.94f)
        val radius = minOf(maxRadiusByHeight, maxRadiusByWidth).coerceAtLeast(0f)

        val cx = w / 2f
        val cy = h - endOffsetRatio * radius
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (arcRect.isEmpty) return

        // 先铺满整条轨道，再把已完成的部分盖上去
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, trackPaint)
        if (progress > 0f) {
            canvas.drawArc(arcRect, startAngle, sweepAngle * progress, false, progressPaint)
        }
    }
}