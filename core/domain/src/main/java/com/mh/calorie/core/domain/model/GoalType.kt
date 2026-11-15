package com.mh.calorie.core.domain.model

/**
 * 目标类型：这款 App 的核心是三向，不是只有减脂。
 * 每种目标对应不同的热量修正系数，M5 的热量引擎会基于它计算每日配额。
 */
enum class GoalType {
    /** 减脂：TDEE 基础上做热量缺口 */
    LOSE_WEIGHT,

    /** 维持：热量 = TDEE */
    MAINTAIN,

    /** 增重/增肌：TDEE 基础上做热量盈余（你截图里用户就是这个目标） */
    GAIN_WEIGHT
}