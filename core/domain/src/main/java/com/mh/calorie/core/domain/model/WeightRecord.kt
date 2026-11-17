package com.mh.calorie.core.domain.model

import java.time.LocalDate

/**
 * 一次体重打卡。纯数据、零 Android 依赖，M4 建 Room 表时这个类直接当实体字段来源。
 * 只存 date + kg，不做「同一天多条」的合并，去重规则放在仓库层。
 */
data class WeightRecord (
    val date: LocalDate,
    val weightKg: Float
)