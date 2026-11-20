package com.mh.calorie.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 一条喝水记录。日历里的水位是这些记录按天聚合出来的，不单独存。
 * [ratio] 是含水量（纯水 100%，茶/咖啡等以后可调低）。
 */
data class WaterRecord (
    val recordedAt: LocalDateTime,
    val ml: Int,
    val ratio: Int = 100
) {
    val date: LocalDate get() = recordedAt.toLocalDate()

    /** 净水量 = 毫升 × 含水量 */
    val netMl: Int get() = ml * ratio / 100
}