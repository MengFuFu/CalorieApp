package com.mh.calorie.ui.weight.body

import com.mh.calorie.core.domain.model.BodyPart

/** 网格里的一格：部位 + 最新数值（null = 还没记录过） */
data class BodyPartRow(
    val part: BodyPart,
    val valueCm: Float?
)


data class BodyUiState(
    val rows: List<BodyPartRow> = emptyList(),
    /** 历史卡里当前选中的部位 */
    val selectedPart: BodyPart = BodyPart.WAIST,
    /** 近一月 = 30，近一周 = 7 */
    val rangeDays: Int = 30
)