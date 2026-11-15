package com.mh.calorie

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 全局 Application。
 *
 * @HiltAndroidApp 会在这里生成 Hilt 的组件容器（SingletonComponent），
 * 之后所有 @AndroidEntryPoint 的 Activity/Fragment 才能拿到依赖注入。
 */
@HiltAndroidApp
class CalorieApp : Application()