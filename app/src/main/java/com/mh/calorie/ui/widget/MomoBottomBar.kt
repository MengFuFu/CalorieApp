package com.mh.calorie.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.mh.calorie.R
import com.mh.calorie.databinding.ViewBottomBarBinding
import kotlinx.coroutines.selects.select

/**
 * 自绘底部导航栏：4 个 Tab + 中间上浮 FAB。
 *
 * 为什么不用 BottomNavigationView：
 * 它把「每项高度、图标大小、label 字号」都写死在内部 style 里，而且不支持中间留空 + 上浮按钮，
 * 强行改要反射或大量 style 覆盖。这个栏的 4 个 Tab 是完全静态的，自己拼反而只有几十行。
 *
 * 这个类只负责「长什么样 + 点哪个变哪个」，不碰导航逻辑；
 * 选 Tab 之后干什么由外部通过 [onTabSelected] 决定，方便以后换掉底栏而不动导航。
 */
class MomoBottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class Tab { RECORD, PLAN, STATS, PROFILE }

    // 注意：binding / entries 必须声明在 init 之前，Kotlin 的属性初始化按声明顺序执行，
    // 放在 init 后面会导致 init 里访问到 null。
    private val binding = ViewBottomBarBinding.inflate(LayoutInflater.from(context), this)

    /** 把「Tab 枚举 → 三个 View」绑在一起，省掉四份散落的 when 判断 */
    private val entries: List<TabEntry> = listOf(
        TabEntry(Tab.RECORD, binding.tabRecord, binding.iconRecord, binding.labelRecord),
        TabEntry(Tab.PLAN, binding.tabPlan, binding.iconPlan, binding.labelPlan),
        TabEntry(Tab.STATS, binding.tabStats, binding.iconStats, binding.labelStats),
        TabEntry(Tab.PROFILE, binding.tabProfile, binding.iconProfile, binding.labelProfile)
    )

    /** 点了某个 Tab（同一 Tab 重复点击也会回调，是否需要忽略由调用方决定） */
    var onTabSelected: ((Tab) -> Unit)? = null

    /** 点了中间的加号 */
    var onAddClick: (() -> Unit)? = null

    init {
        // 白底：底栏的 padding 区域也会被背景覆盖，系统导航条背后同样是白色
        setBackgroundColor(ContextCompat.getColor(context, R.color.surface_card))

        // 系统导航条高度由底栏自己消化（加在 padding 上），
        // 这样底栏总高度自动变高，上面的页面内容不需要知道导航条的存在。
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            view.updatePadding(
                bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            )
            insets
        }

        entries.forEach { entry ->
            entry.container.setOnClickListener {
                select(entry.tab)
                onTabSelected?.invoke(entry.tab)
            }
        }

        // 加号就在底栏内部（layout_gravity=center），不再需要上浮位移
        binding.fabAdd.setOnClickListener { onAddClick?.invoke() }

        // 初始选中「记录」，保证第一帧就有正确的视觉状态
        select(Tab.RECORD)
    }

    /**
     * 只改视觉状态，不触发 [onTabSelected]。
     * 导航回写选中态时必须用这个（否则会变成「选中→导航→再选中」的无限循环）。
     */
    fun select(tab: Tab) {
        entries.forEach { entry ->
            val selected = entry.tab == tab
            // container 负责无障碍的 selected 语义，icon/label 各自持有状态色 selector
            entry.container.isSelected = selected
            entry.icon.isSelected = selected
            entry.label.isSelected = selected
        }
    }

    private data class TabEntry(
        val tab: Tab,
        val container: View,
        val icon: ImageView,
        val label: TextView
    )
}