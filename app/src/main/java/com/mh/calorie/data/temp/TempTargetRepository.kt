package com.mh.calorie.data.temp

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 每日目标的临时存放处。
 *
 * 现在直接用设计稿那组数字；M5 做完「档案 + 热量引擎」后，
 * 这里会改成按 BMR × PAL × 目标类型现算，并把结果缓存进来。
 * 用 var 是为了以后设置页能改；改成 StateFlow 后首页可实时刷新。
 */
@Singleton
class TempTargetRepository @Inject constructor() {

    /** 每日热量目标 kcal */
    var targetCalorie: Float = 2204f

    /** 三大宏量目标（克） */
    var carbTargetG: Float = 284.76f
    var proteinTargetG: Float = 114.96f
    var fatTargetG: Float = 68.77f

}