package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mh.calorie.R
import com.mh.calorie.core.domain.bmi.BmiCalculator
import java.util.Locale

/**
 * BMI 四段色卡 + 当前位置三角标。
 * 分段比例由量程 (SCALE_MIN..SCALE_MAX) 线性算出，不写死百分比，
 * 这样以后调阈值时标签位置会跟着动，不会错位。
 */
class BmiScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.text_primary)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.text_tertiary)
    }

    private val rect = RectF()
    private val marker = Path()

    var bmi: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0) return

        val barTop = dp(12f)
        val barH = dp(8f)
        val gap = dp(2f)
        val radius = barH / 2f

        val segments: List<Triple<Float, Float, Int>> = listOf(
            Triple(BmiCalculator.SCALE_MIN, BmiCalculator.LOW_MAX, R.color.bmi_low),
            Triple(BmiCalculator.LOW_MAX, BmiCalculator.NORMAL_MAX, R.color.bmi_normal),
            Triple(BmiCalculator.NORMAL_MAX, BmiCalculator.OVERWEIGHT_MAX, R.color.bmi_over),
            Triple(BmiCalculator.OVERWEIGHT_MAX, BmiCalculator.SCALE_MAX, R.color.bmi_obese)
        )

        segments.forEach { (from, to, color) ->
            barPaint.color = ContextCompat.getColor(context, color)
            rect.set(xOf(from) + gap / 2f, barTop, xOf(to) - gap / 2f, barTop + barH)
            canvas.drawRoundRect(rect, radius, radius, barPaint)
        }

        // 当前 BMI 的三角标（从上往下指）
        if (bmi > 0f) {
            val cx = xOf(bmi).coerceIn(dp(5f), width - dp(5f))
            marker.reset()
            marker.moveTo(cx - dp(5f), barTop - dp(7f))
            marker.lineTo(cx + dp(5f), barTop - dp(7f))
            marker.lineTo(cx, barTop - dp(1f))
            marker.close()
            canvas.drawPath(marker, markerPaint)
        }

        // 分界数值
        listOf(
            BmiCalculator.LOW_MAX,
            BmiCalculator.NORMAL_MAX,
            BmiCalculator.OVERWEIGHT_MAX
        ).forEach {
            canvas.drawText(
                String.format(Locale.US, "%.1f", it),
                xOf(it),
                barTop + barH + dp(13f),
                labelPaint
            )
        }
    }

    private fun xOf(value: Float): Float {
        val span = BmiCalculator.SCALE_MAX - BmiCalculator.SCALE_MIN
        val ratio = ((value - BmiCalculator.SCALE_MIN) / span).coerceIn(0f, 1f)
        return ratio * width
    }
}