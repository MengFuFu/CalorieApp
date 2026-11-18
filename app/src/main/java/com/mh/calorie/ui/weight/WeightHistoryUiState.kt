package com.mh.calorie.ui.weight

/** 卡片里的一行：时间 + 体重 + 体脂率（可能没记） */
data class WeightHistoryRow(
    val time: String,
    val weightKg: Float,
    val bodyFatPercent: Float?
)

/** 一天 = 一张卡 */
data class WeightDayGroup(
    val dateText: String,
    val rows: List<WeightHistoryRow>
)

/** 历史体重列表页的渲染状态 */
data class WeightHistoryUiState(
    val monthText: String = "",
    val groups: List<WeightDayGroup> = emptyList(),
    val isEmpty: Boolean = true,
    /** 已经到当前月就不许再往后翻 */
    val canGoNext: Boolean = false
)