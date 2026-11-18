package com.mh.calorie.core.domain.model

import java.time.LocalDateTime


/**
 * 一条围度记录。同一部位一天可以有多条，展示时取最新那条。
 */
data class BodyMeasurement(
    val recordedAt: LocalDateTime,
    val part: BodyPart,
    val valueCm: Float
) {
    val date get() = recordedAt.toLocalDate()
}