package com.mh.calorie.core.common

/**
 * 全局常量。
 * 集中放在这里，避免日期格式、单位换算在不同页面各写一遍导致不一致。
 */
object Constants {

    /** 统一的日期格式：本地存储与接口传输都用这个 */
    const val DATE_PATTERN = "yyyy-MM-dd"

    /** 大卡与千焦的换算：1 kcal = 4.184 kJ，国内营养成分表多用 kJ */
    const val KJ_PER_KCAL = 4.184

    /** 三大宏量每克提供的热量（kcal/g），热量引擎计算会用到 */
    const val KCAL_PER_GRAM_CARB = 4.0
    const val KCAL_PER_GRAM_PROTEIN = 4.0
    const val KCAL_PER_GRAM_FAT = 9.0

    /** 餐次定义，顺序即 UI 展示顺序 */
    val MEAL_TYPES = listOf("早餐", "午餐", "晚餐", "加餐")
}