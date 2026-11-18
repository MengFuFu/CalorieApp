package com.mh.calorie.core.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 一次体重打卡。纯数据、零 Android 依赖，M4 建 Room 表时直接当实体字段来源。
 * 时间精确到分钟：历史列表要显示 HH:mm，同一天可以记多次。
 */
data class WeightRecord (
    val recordedAt: LocalDateTime,
    val weightKg: Float
) {
    /** 归属日期，图表按天聚合时用 */
    val date: LocalDate get() = recordedAt.toLocalDate()
}