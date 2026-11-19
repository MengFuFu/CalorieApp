package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.SportRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** 运动记录临时仓库（M4 接 Room 后整类删除） */
@Singleton
class TempSportRecordRepository @Inject constructor() {

    private val _records = MutableStateFlow(seed())
    val records: StateFlow<List<SportRecord>> = _records.asStateFlow()

    fun all(): List<SportRecord> = _records.value

    fun add(
        recordedAt: LocalDateTime,
        name: String,
        quantity: Float,
        unitLabel: String,
        kcal: Float
    ) {
        // 同一秒内连续保存时把时间顶开，避免排序不稳定
        var at = recordedAt
        while (_records.value.any { it.recordedAt == at }) {
            at = at.plusSeconds(1)
        }
        _records.value = (_records.value + SportRecord(at, name, quantity, unitLabel, kcal))
            .sortedBy { it.recordedAt }
    }

    /** 两条今天的种子数据，便于先看到分组效果 */
    private fun seed(): List<SportRecord> {
        val today = LocalDate.now()
        return listOf(
            SportRecord(today.atTime(8, 12), "散步", 10f, "min", 27f),
            SportRecord(today.atTime(9, 40), "跑步（快速）", 10f, "min", 90f)
        )
    }
}