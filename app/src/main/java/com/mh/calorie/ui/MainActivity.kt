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
import com.mh.calorie.data.temp.TempAuthRepository
import jakarta.inject.Inject

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

    /** 启动闸门开关：false = 启动直接进首页；等后端鉴权接好再改 true */
    private val authGateEnabled = false

    /** 状态栏高度：insets 回调里更新，切页时复用 */
    private var statusBarTop = 0

    // 临时账号仓库：M15 换后端后仍然注入，只是实现换掉
    @Inject
    lateinit var authRepo: TempAuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyStatusBarInset()
        setupNavigation()

        // 未登录先弹登录页；savedInstanceState != null 说明是旋转重建，别重复压栈
        if (authGateEnabled && savedInstanceState == null && !authRepo.isLoggedIn) {
            navController.navigate(R.id.action_homeFragment_to_loginFragment)
        }
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            statusBarTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            applyTopInsetToPage()
            insets
        }
    }

    /**
     * 把状态栏高度加到「当前页面的根 View」上，而不是整棵 Activity 根布局。
     * 页面自己的 background 会一直铺到状态栏下面，只有内容靠 padding 让开，
     * 状态栏那块就不会露出 windowBackground 形成色差断层。
     */
    private fun applyTopInsetToPage() {
        val host = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as? NavHostFragment ?: return
        val page = host.childFragmentManager.fragments
            .firstOrNull { it.view != null }?.view ?: return

        page.post {
            if (page.paddingTop != statusBarTop) page.updatePadding(top = statusBarTop)
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTab = destination.id.isTopLevelTab()
            binding.bottomBar.isVisible = isTab
            if (isTab) binding.bottomBar.select(destination.id.toTab())

            // 新页面可能还没 attach 完，等一帧再补 padding
            binding.root.post { applyTopInsetToPage() }
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