package com.mh.calorie.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 一条食物记录。
 * 宏量存的是**快照**：以后食物库的营养数据改了，历史记录也不会跟着变。
 */
data class FoodLog(
    val recordedAt: LocalDateTime,
    val meal: MealType,
    val name: String,
    /** 输入的份量，如 "100" */
    val quantity: String,
    /** 份量单位，如 "g" / "个" */
    val unit: String,
    val kcal: Float,
    val carbGram: Float,
    val proteinGram: Float,
    val fatGram: Float
) {
    val date: LocalDate get() = recordedAt.toLocalDate()
}