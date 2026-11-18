package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.mh.calorie.R
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

/**
 * 体重折线图。设计稿里没有 y 轴刻度，所以就画：一条淡色顶线 + 折线 + 空心圆点 + 日期 + 点选气泡。
 * 点选交互用 onTouchEvent 自己做，不引第三方图表库（Vico 只支持 Compose）。
 */
class WeightChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Point(val date: LocalDate, val label: String, val value: Float)

    /** 气泡里数值前的文字：体重页「体重」，围度页传「腰围」「胸围」… */
    var valuePrefix: String = "体重"

    /** y 轴最小跨度：体重用 1（kg），围度用 2（cm），避免一条直线贴底 */
    var minSpan: Float = 1f

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity

    private val green = ContextCompat.getColor(context, R.color.brand_green)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = green
    }
    private val dotRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
        color = green
    }
    private val dotFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = ContextCompat.getColor(context, R.color.chart_guide)
        pathEffect = DashPathEffect(floatArrayOf(dp(3f), dp(3f)), 0f)
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(10f)
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.text_tertiary)
    }
    private val tipTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(11f)
        color = ContextCompat.getColor(context, R.color.text_secondary)
    }
    private val tipValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(12f)
        typeface = Typeface.DEFAULT_BOLD
        color = ContextCompat.getColor(context, R.color.text_primary)
    }
    private val tipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val tipBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = ContextCompat.getColor(context, R.color.divider)
    }

    private val linePath = Path()
    private val tipPath = Path()
    private val tipRect = RectF()
    private val plot = RectF()

    private var points: List<Point> = emptyList()
    private var selectedIndex = -1
    private var curMin = 0f
    private var curMax = 1f

    fun submit(points: List<Point>) {
        this.points = points
        selectedIndex = if (points.isEmpty()) -1 else points.lastIndex
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty() || width == 0) return

        val top = dp(52f)               // 上方留出气泡高度
        val bottom = height - dp(20f)   // 下方留出日期
        plot.set(dp(8f), top, width - dp(8f), bottom)

        // y 轴范围：数据上下各留 12%，最小跨度 1kg，避免一条水平线贴在底部
        var min = points.minOf { it.value }
        var max = points.maxOf { it.value }
        if (max - min < minSpan) {
            val c = (min + max) / 2f
            min = c - minSpan / 2f
            max = c + minSpan / 2f
        }
        val extra = (max - min) * 0.12f
        curMin = min - extra
        curMax = max + extra

        // 顶部淡色参考线
        canvas.drawLine(plot.left, plot.top - dp(14f), plot.right, plot.top - dp(14f), guidePaint)

        // 折线
        linePath.reset()
        points.forEachIndexed { i, p ->
            val x = xOf(i)
            val y = yOf(p.value)
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        canvas.drawPath(linePath, linePaint)

        // 圆点 + 日期（按可用宽度抽稀，30 天也不会叠字）
        var lastLabelRight = -Float.MAX_VALUE
        points.forEachIndexed { i, p ->
            val x = xOf(i)
            val y = yOf(p.value)
            canvas.drawCircle(x, y, dp(4f), dotFillPaint)
            canvas.drawCircle(x, y, dp(4f), dotRingPaint)

            val labelW = axisPaint.measureText(p.label)
            val fits = x - labelW / 2f > lastLabelRight + dp(8f)
            if (fits || i == points.lastIndex) {
                canvas.drawText(p.label, x, height - dp(3f), axisPaint)
                lastLabelRight = x + labelW / 2f
            }
        }

        if (selectedIndex in points.indices) drawTooltip(canvas)
    }

    private fun drawTooltip(canvas: Canvas) {
        val p = points[selectedIndex]
        val px = xOf(selectedIndex)
        val py = yOf(p.value)

        val padH = dp(10f)
        val padV = dp(6f)
        val title = p.label
        val value = "$valuePrefix " + formatValue(p.value)

        val w = maxOf(tipTitlePaint.measureText(title), tipValuePaint.measureText(value)) + padH * 2
        val h = dp(34f) + padV * 2

        val x = (px - w / 2f).coerceIn(plot.left, (plot.right - w).coerceAtLeast(plot.left))
        val y = (py - dp(12f) - h).coerceAtLeast(0f)

        tipRect.set(x, y, x + w, y + h)

        // 先画小三角，再用圆角矩形盖住它的上边，边框才不会出现一条横线
        val ax = px.coerceIn(x + dp(10f), x + w - dp(10f))
        tipPath.reset()
        tipPath.moveTo(ax - dp(5f), y + h - dp(1f))
        tipPath.lineTo(ax + dp(5f), y + h - dp(1f))
        tipPath.lineTo(ax, y + h + dp(5f))
        tipPath.close()
        canvas.drawPath(tipPath, tipBgPaint)
        canvas.drawPath(tipPath, tipBorderPaint)

        canvas.drawRoundRect(tipRect, dp(8f), dp(8f), tipBgPaint)
        canvas.drawRoundRect(tipRect, dp(8f), dp(8f), tipBorderPaint)

        canvas.drawText(title, x + padH, y + padV + dp(11f), tipTitlePaint)
        canvas.drawText(value, x + padH, y + padV + dp(28f), tipValuePaint)
    }

    private fun xOf(index: Int): Float {
        if (points.size == 1) return plot.centerX()
        return plot.left + plot.width() * index / (points.size - 1)
    }

    private fun yOf(value: Float): Float {
        val span = (curMax - curMin).takeIf { it > 0f } ?: 1f
        return plot.bottom - (value - curMin) / span * plot.height()
    }

    private fun formatValue(v: Float): String =
        if (v == v.toInt().toFloat()) String.format(Locale.US, "%.0f", v)
        else String.format(Locale.US, "%.1f", v)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (points.isEmpty()) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val idx = nearestIndex(event.x)
                if (idx >= 0) {
                    selectedIndex = idx
                    invalidate()
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun nearestIndex(x: Float): Int {
        var best = -1
        var bestDist = Float.MAX_VALUE
        points.indices.forEach { i ->
            val d = abs(xOf(i) - x)
            if (d < bestDist) {
                bestDist = d
                best = i
            }
        }
        return best
    }
}