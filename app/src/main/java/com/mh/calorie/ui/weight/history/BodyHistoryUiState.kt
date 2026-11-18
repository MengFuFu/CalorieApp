package com.mh.calorie.ui.weight.history

import com.mh.calorie.core.domain.model.BodyPart

/** 卡片里的一行：时刻 + 部位 + 数值（部位名/图标由 Adapter 用 Context 解析，
 *  UiState 不持有 Context） */
data class BodyHistoryRow(
    val time: String,
    val part: BodyPart,
    val valueCm: Float
)

/** 一天 = 一张卡 */
data class BodyDayGroup(
    val dateText: String,
    val rows: List<BodyHistoryRow>
)

/** 历史围度列表页的渲染状态 */
data class BodyHistoryUiState (
    val monthText: String = "",
    val groups: List<BodyDayGroup> = emptyList(),
    val isEmpty: Boolean = true,
    val canGoNext: Boolean = false
)