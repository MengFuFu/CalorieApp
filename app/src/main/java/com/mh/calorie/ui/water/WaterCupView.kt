package com.mh.calorie.ui.water

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mh.calorie.R


/**
 * 日历里的小水杯。
 * 杯身是一个上宽下窄、底部圆角的梯形；水位用 clipPath 裁出杯形后从底部往上填蓝色。
 * [setRatio] 传 0f~1f：0 空杯、1 满杯。
 */
class WaterCupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var ratio = 0f

    private val cupPath = Path()

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.water_blue_surface)
    }
    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.water_blue)
    }

    fun setRatio(value: Float) {
        val safe = value.coerceIn(0f, 1f)
        if(safe == ratio) return
        ratio = safe
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // 杯口留一点空隙，看起来像有杯壁厚度
        val top = h * 0.10f
        val bottomR = w * 0.16f
        val leftTop = w * 0.05f
        val rightTop = w * 0.95f
        val leftBottom = w * 0.16f
        val rightBottom = w * 0.84f

        cupPath.reset()
        cupPath.moveTo(leftTop, top)
        cupPath.lineTo(rightTop, top)
        cupPath.lineTo(rightBottom, h - bottomR)
        cupPath.quadTo(rightBottom, h, rightBottom - bottomR, h)
        cupPath.lineTo(leftBottom + bottomR, h)
        cupPath.quadTo(leftBottom, h, leftBottom, h - bottomR)
        cupPath.close()

        // 空杯底色
        canvas.drawPath(cupPath, emptyPaint)

        if (ratio > 0f) {
            canvas.save()
            canvas.clipPath(cupPath)
            val waterTop = h - (h - top) * ratio
            canvas.drawRect(0f, waterTop, w, h, waterPaint)
            canvas.restore()
        }
    }

}