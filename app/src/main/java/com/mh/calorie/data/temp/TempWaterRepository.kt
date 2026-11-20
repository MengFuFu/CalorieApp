package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.WaterRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** 喝水临时仓库（M4 接 Room 后整类删除）。日历与首页都订阅这里的 records。 */
@Singleton
class TempWaterRepository @Inject constructor() {

    private val _records = MutableStateFlow<List<WaterRecord>>(emptyList())
    val records: StateFlow<List<WaterRecord>> = _records.asStateFlow()

    /** 每日目标（后面接档案里算出来的值） */
    var goalMl: Int = 1650

    fun all(): List<WaterRecord> = _records.value

    /** 某天的记录，时间倒序（最新的在最上面） */
    fun ofDate(date: LocalDate): List<WaterRecord> =
        _records.value.filter { it.date == date }.sortedByDescending { it.recordedAt }

    /** 某天的净水量合计——日历水杯的水位就靠它 */
    fun netOn(date: LocalDate): Int =
        _records.value.filter { it.date == date }.sumOf { it.netMl }

    /** 水位 0f~1f，供 WaterCupView 用 */
    fun ratioOn(date: LocalDate): Float {
        if (goalMl <= 0) return 0f
        return (netOn(date).toFloat() / goalMl).coerceIn(0f, 1f)
    }

    fun add(recordedAt: LocalDateTime, ml: Int, ratio: Int = 100) {
        // 同一秒连点两次时把时间顶开，保证排序稳定
        var at = recordedAt
        while (_records.value.any { it.recordedAt == at }) {
            at = at.plusSeconds(1)
        }
        _records.value = (_records.value + WaterRecord(at, ml, ratio))
            .sortedBy { it.recordedAt }
    }
}