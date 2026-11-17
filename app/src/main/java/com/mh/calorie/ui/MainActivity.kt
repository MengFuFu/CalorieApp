package com.mh.calorie.ui

import android.os.Bundle
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.mh.calorie.R
import com.mh.calorie.databinding.ActivityMainBinding
import com.mh.calorie.ui.widget.MomoBottomBar
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import androidx.core.view.isVisible

/**
 * 全局唯一的 Activity：只做三件事
 *   1. 承载 NavHost（页面全在 Fragment 里）
 *   2. 把系统栏 inset 一次处理干净
 *   3. 把底栏点击翻译成导航动作
 *
 * 页面自己的逻辑、ViewModel、数据绑定一律不下放到这里，避免 Activity 越写越胖。
 */
@AndroidEntryPoint // 让 Hilt 能向本 Activity 注入依赖，现在没依赖可注入，但有它才能验证 DI 通了
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyStatusBarInset()
        setupNavigation()
    }

    /**
     * 状态栏 inset：加在根布局的 paddingTop 上。
     *
     * 为什么不用 android:fitsSystemWindows：
     * 它只对单个 View 生效，遇到滚动容器、Fragment 切换时行为很难预测；
     * 也没法做到「背景铺满、内容让开」这种效果。
     *
     * 为什么只处理 top、不处理 bottom：
     * 底部是底栏的地盘，底栏自己把导航条高度加进 padding（见 MomoBottomBar），
     * 那里已经处理过一次，如果这里再加就会多出一截空白。
     */
    private fun applyStatusBarInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) {
            view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            view.updatePadding(top = top)
            insets // 不消费，让底栏也能收到同一份 inset
        }
    }

    private fun setupNavigation() {
        val host = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = host.navController

        // 底栏 → 导航。Tab 的视觉状态不在这里改，交给下面的目的地监听统一回写。
        binding.bottomBar.onTabSelected = {tab -> navigateTo(tab)}

        binding.bottomBar.onAddClick = {
            // 中间的加号 = 和 App 内置 AI 聊天（不是快速记饮食）。
            // 聊天页在 M12 做，先给提示占位。
            Toast.makeText(
                this,
                getString(R.string.toast_coming_soon, getString(R.string.ai_chat), "M12"),
                Toast.LENGTH_SHORT
            ).show()
        }
        // 导航 → 底栏。以「当前目的地」为唯一真值来源，
        // 这样系统返回键、进程恢复、深链接改动页面时，底栏永远是对的。
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 体重详情这类二级页不显示底栏；Tab 页才显示。
            // 必须在这里同步隐藏，否则进二级页后底栏还亮着「记录」，用户会以为能点
            val isTab = destination.id.isTopLevelTab()
            binding.bottomBar.isVisible = isTab
            if (isTab) binding.bottomBar.select(destination.id.toTab())
        }
    }

    private fun navigateTo(tab: MomoBottomBar.Tab) {
        val destId = tab.toDestinationId()

        // 已在当前页就什么都不做：否则每次点击都会重建 Fragment，状态全丢
        if(navController.currentDestination?.id == destId) return

        val options = NavOptions.Builder()
        // 同一目的地不重复入栈
            .setLaunchSingleTop(true)
        // 切回来时恢复上次的 View 状态和返回栈（配合 openTask 效果）
            .setRestoreState(true)
        // 每次都弹回起始页再进新 Tab：Tab 之间来回点不会把返回栈堆成一条长链，
        // saveState=true 让被弹掉的页面状态暂存，切回去能还原滚动位置
            .setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = true,
                saveState = true
            )
            .build()

        navController.navigate(destId, null, options)
    }

    /** Tab → 导航目的地 id。新增 Tab 时只改这里和枚举 */
    private fun MomoBottomBar.Tab.toDestinationId(): Int = when(this) {
        MomoBottomBar.Tab.RECORD -> R.id.homeFragment
        MomoBottomBar.Tab.PLAN -> R.id.planFragment
        MomoBottomBar.Tab.STATS -> R.id.statsFragment
        MomoBottomBar.Tab.PROFILE -> R.id.profileFragment
    }

    /** 导航目的地 id → Tab。未知目的地一律落到「记录」，不会出现所有 Tab 都不选中的情况 */
    private fun Int.toTab(): MomoBottomBar.Tab = when(this) {
        R.id.planFragment -> MomoBottomBar.Tab.PLAN
        R.id.statsFragment -> MomoBottomBar.Tab.STATS
        R.id.profileFragment -> MomoBottomBar.Tab.PROFILE
        else -> MomoBottomBar.Tab.RECORD
    }

    private fun Int.isTopLevelTab(): Boolean =
        this == R.id.homeFragment ||
                this == R.id.planFragment ||
                this == R.id.statsFragment ||
                this == R.id.profileFragment
}