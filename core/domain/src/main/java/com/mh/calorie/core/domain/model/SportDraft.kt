package com.mh.calorie.core.domain.model

import java.time.LocalDateTime

/** 「已添加但还没保存」的运动草稿；点运动库的「完成」时批量写进 SportRecord */
data class SportDraft(
    val id: Long,
    val name: String,
    val quantity: Float,
    val unitLabel: String,
    val kcal: Float,
    val recordedAt: LocalDateTime
)