package com.mh.calorie.ui.sport


/** 一条运动记录（已格式化成字符串，Adapter 直接用） */
data class SportRecordRow(
    val timeText: String,
    val name: String,
    val quantityText: String,
    val kcalText: String
)

/** 一天的记录 */
data class SportDayGroup(
    val dateText: String,
    val rows: List<SportRecordRow>
)

data class SportRecordUiState (
    val monthText: String = "",
    val totalText: String = "0",
    val groups: List<SportDayGroup> = emptyList(),
    val isEmpty: Boolean = true,
    val canGoNext: Boolean = false
)