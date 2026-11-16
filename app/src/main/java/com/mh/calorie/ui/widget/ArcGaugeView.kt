package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mh.calorie.R

/**
 * 首页弧形仪表。
 *
 * 设计稿参数（按 1280px 设计稿 × 3.114 换算）：
 *   半径 142dp、线宽 14dp、从 172° 顺时针扫 196°（两端比水平线低 8°），两端渐隐。
 * 半径由宽高共同约束，具体见 onSizeChanged。
 */
class ArcGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeWidthPx = 14f * resources.displayMetrics.density

    // 172° 起、196° 止：左右两端各沉到水平线下方 8°，端头才像设计稿那样垂下来
    private val startAngle = 172f
    private val sweepAngle = 196f

    private val endOffsetRatio = 0.1392f // sin(8°)
    private val endXDxRatio = 0.9903f    // cos(8°)

    /** 几何就绪回调：endDx = 弧端相对控件中心的水平偏移，endY = 弧端圆心线的 y */
    var onArcMeasured: ((endDx: Float, endY: Float) -> Unit)? = null

    private val trackColor = ContextCompat.getColor(context, R.color.arc_track)
    private val progressColor = ContextCompat.getColor(context, R.color.brand_green)

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = strokeWidthPx
        color = trackColor
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = strokeWidthPx
        color = progressColor
    }

    private val arcRect = RectF()
    private var progress = 0f

    /** 0f~1f，超出范围会被夹紧 */
    fun setProgress(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        if (clamped == progress) return
        progress = clamped
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 半径受两件事约束：
        //   1) 高度：弧顶到两端要占 r*(1+sin8°)，再加线宽
        //   2) 宽度：两端横向展开 2*cos(8°)*r
        val maxRadiusByHeight = (h - strokeWidthPx) / (1f + endOffsetRatio)
        val maxRadiusByWidth = (w - strokeWidthPx) / (2f * endXDxRatio)
        val radius = minOf(maxRadiusByHeight, maxRadiusByWidth).coerceAtLeast(0f)

        val cx = w / 2f
        // 再往上让出 strokeWidthPx/2，圆头端帽才不会被控件底边切平
        val cy = h - endOffsetRatio * radius - strokeWidthPx / 2f
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // 竖直方向的绿→透明渐变：弧的两端自然淡出，对应设计稿那种“渐隐收尾”
        if (h > 0) {
            trackPaint.shader = verticalFade(trackColor, h)
            progressPaint.shader = verticalFade(progressColor, h)
        }

        onArcMeasured?.invoke(endXDxRatio * radius, cy + endOffsetRatio * radius)
    }

    /** 顶部实色、55% 处仍实色、底部全透明 */
    private fun verticalFade(color: Int, height: Int): Shader = LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        intArrayOf(color, color, Color.TRANSPARENT),
        floatArrayOf(0f, 0.55f, 1f),
        Shader.TileMode.CLAMP
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (arcRect.isEmpty) return

        canvas.drawArc(arcRect, startAngle, sweepAngle, false, trackPaint)
        if (progress > 0f) {
            canvas.drawArc(arcRect, startAngle, sweepAngle * progress, false, progressPaint)
        }
    }
}