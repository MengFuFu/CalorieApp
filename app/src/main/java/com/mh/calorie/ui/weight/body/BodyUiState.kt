package com.mh.calorie.ui.weight.body

import android.R
import com.mh.calorie.core.domain.model.BodyPart
import java.time.LocalDate

/** 网格里的一格：部位 + 最新数值（null = 还没记录过） */
data class BodyPartRow(
    val part: BodyPart,
    val valueCm: Float?
)

/** 折线图上的一个点；label 是 x 轴文字（如 11/18） */
data class BodyChartPoint(
    val date: LocalDate,
    val label: String,
    val valueCm: Float
)

data class BodyUiState(
    val rows: List<BodyPartRow> = emptyList(),
    /** 历史卡里当前选中的部位 */
    val selectedPart: BodyPart = BodyPart.WAIST,
    /** 近一月 = 30，近一周 = 7 */
    val rangeDays: Int = 30,
    /** 当前部位 + 区间下的折线数据，空表示这段时间没记录 */
    val chartPoints: List<BodyChartPoint> = emptyList()
)