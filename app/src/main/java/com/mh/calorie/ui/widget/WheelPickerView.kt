package com.mh.calorie.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.mh.calorie.R
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 竖排滚轮选择器（全自绘）。
 *
 * 不用 NumberPicker 的原因：它自带蓝色分隔线、上下箭头和固定行高，
 * 做不出「选中行圆角胶囊 + 上下渐隐 + 无分隔线」的设计。
 *
 * 屏幕里永远只显示 5 行，高度由 onMeasure 写死，保证三个滚轮横向对齐。
 */
class WheelPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private val itemHeight = resources.getDimension(R.dimen.wheel_item_height)
    private val pillRadius = resources.getDimension(R.dimen.wheel_pill_radius)
    private val pillInset = 4f * density

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.wheel_highlight)
    }
    private val pillRect = RectF()

    @ColorInt
    private val colorSelected = ContextCompat.getColor(context, R.color.text_primary)
    @ColorInt
    private val colorNormal = ContextCompat.getColor(context, R.color.wheel_text_normal)

    private val textSizeSelected = 17f * scaledDensity
    private val textSizeNormal = 15f * scaledDensity

    private var items: List<String> = emptyList()

    /** 滚动偏移（px）：0 = 第一项居中；(size-1)*itemHeight = 最后一项居中 */
    private var scrollOffset = 0f
    private val maxOffset: Float get() = max(0f, (items.size - 1) * itemHeight)

    private val scroller = OverScroller(context)
    private var tracker: VelocityTracker? = null
    private var lastY = 0f
    private var dragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    /** 选中项落定后回调（拖动过程中不触发） */
    var onSelected: ((Int) -> Unit)? = null

    val selectedIndex: Int
        get() = if (items.isEmpty()) 0
        else (scrollOffset / itemHeight).roundToInt().coerceIn(0, items.lastIndex)

    fun setItems(labels: List<String>, selected: Int = 0) {
        items = labels
        scrollOffset = selected.coerceIn(0, max(0, labels.lastIndex)) * itemHeight
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 高度恒等于 5 行，忽略 XML 里给的 height，避免三个滚轮高度不一致
        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            (itemHeight * VISIBLE_ROWS).toInt()
        )
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollOffset = scroller.currY.toFloat().coerceIn(0f, maxOffset)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty()) return

        val centerY = height / 2f

        // 选中行的高亮胶囊，上下各让出 6% 行高，看起来更细一点
        val halfFill = itemHeight * 0.44f
        pillRect.set(pillInset, centerY - halfFill, width - pillInset, centerY + halfFill)
        canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillPaint)

        val centerIndex = scrollOffset / itemHeight
        val first = floor(centerIndex).toInt() - VISIBLE_ROWS / 2 - 1
        val last = first + VISIBLE_ROWS + 2

        for (i in max(0, first)..min(items.lastIndex, last)) {
            val rowCenterY = centerY + (i * itemHeight - scrollOffset)
            val distance = abs(i - centerIndex)

            // 2.6 行以外完全淡出
            val alpha = (1f - distance / 2.6f).coerceIn(0f, 1f)
            if (alpha <= 0f) continue

            val isSelected = distance < 0.5f
            textPaint.textSize = if (isSelected) textSizeSelected else textSizeNormal
            textPaint.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textPaint.color = if (isSelected) colorSelected else colorNormal
            textPaint.alpha = (alpha * 255).toInt()

            // 基线校正：让文字视觉上垂直居中
            val baseline = rowCenterY - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(items[i], width / 2f, baseline, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (items.isEmpty()) return false

        if (tracker == null) tracker = VelocityTracker.obtain()
        tracker?.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scroller.forceFinished(true)
                lastY = event.y
                dragging = false
                // 关键：让 BottomSheetBehavior 的 ViewDragHelper 不要在半路拦截
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = lastY - event.y
                if (!dragging && abs(dy) > touchSlop) dragging = true
                if (dragging) {
                    scrollOffset = (scrollOffset + dy).coerceIn(0f, maxOffset)
                    lastY = event.y
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 松手/被取消后恢复，让 sheet 自己在其它区域还能拖
                parent?.requestDisallowInterceptTouchEvent(false)
                tracker?.computeCurrentVelocity(1000)
                settle(tracker?.yVelocity ?: 0f)
                tracker?.recycle()
                tracker = null
            }
        }
        return true
    }

    /** 松手后吸附到最近一行；快速滑动时朝速度方向多走半格 */
    private fun settle(velocityY: Float) {
        val exact = scrollOffset / itemHeight
        val extra = when {
            velocityY < -FLING_VELOCITY -> 0.5f   // 手指上滑 → 往后翻
            velocityY > FLING_VELOCITY -> -0.5f
            else -> 0f
        }
        val target = (exact + extra).roundToInt().coerceIn(0, items.lastIndex)
        val previous = selectedIndex

        scroller.startScroll(
            0,
            scrollOffset.toInt(),
            0,
            (target * itemHeight - scrollOffset).toInt(),
            SNAP_DURATION_MS
        )
        invalidate()

        if (target != previous) onSelected?.invoke(target)
    }

    private companion object {
        const val VISIBLE_ROWS = 5
        const val SNAP_DURATION_MS = 220
        const val FLING_VELOCITY = 800f
    }
}