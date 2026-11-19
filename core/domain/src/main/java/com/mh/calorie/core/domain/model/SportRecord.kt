package com.mh.calorie.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 一条运动记录。
 * [quantity] + [unitLabel] 是用户填的数量与单位（如 10 + "min"、1 + "局(20min)"、2 + "km"），
 * [kcal] 是当时换算出来的结果——存快照，以后改了运动库的换算规则也不影响历史。
 */
data class SportRecord(
    val recordedAt: LocalDateTime,
    val name: String,
    val quantity: Float,
    val unitLabel: String,
    val kcal: Float
) {
    val date: LocalDate get() = recordedAt.toLocalDate()
}