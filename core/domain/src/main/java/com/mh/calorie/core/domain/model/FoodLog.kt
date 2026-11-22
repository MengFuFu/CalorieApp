package com.mh.calorie.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

/**
 * 一条食物记录 = 「一份」× 份数。
 * 存的是**一份**的基数和份数，所以 −/+ 只改 count，
 * 数量和热量都是算出来的，不会因为四舍五入对不上。
 */
data class FoodLog(
    val id: Long,
    val recordedAt: LocalDateTime,
    val meal: MealType,
    val name: String,
    /** 一份是多少克 / 毫升 / 个 */
    val baseQty: Float,
    val unit: String,
    /** 一份多少千卡 */
    val baseKcal: Float,
    val baseCarb: Float,
    val baseProtein: Float,
    val baseFat: Float,
    /** 份数，最少 1；减到 0 由仓库删除 */
    val count: Int = 1
) {
    val date: LocalDate get() = recordedAt.toLocalDate()

    val kcal: Float get() = baseKcal * count
    val carbGram: Float get() = baseCarb * count
    val proteinGram: Float get() = baseProtein * count
    val fatGram: Float get() = baseFat * count

    /** 「300g」「2个」这种展示文案 */
    val quantityText: String get() = format(baseQty * count) + unit

    companion object {
        fun format(v: Float): String =
            if (v % 1f == 0f) v.toInt().toString()
            else String.format(Locale.US, "%.1f", v)
    }
}