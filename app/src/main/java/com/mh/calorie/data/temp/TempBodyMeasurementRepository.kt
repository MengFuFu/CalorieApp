package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.BodyMeasurement
import com.mh.calorie.core.domain.model.BodyPart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** 临时围度仓库（M4 接 Room 后整类删除） */
@Singleton
class TempBodyMeasurementRepository @Inject constructor() {

    private val _records = MutableStateFlow(seed())
    val records: StateFlow<List<BodyMeasurement>> = _records.asStateFlow()

    /** 追加一条；时间只到分钟，同分钟内多次保存要顺延一秒，避免并列 */
    fun add(recordedAt: LocalDateTime, part: BodyPart, valueCm: Float) {
        var at = recordedAt
        while (_records.value.any { it.recordedAt == at }) {
            at = at.plusSeconds(1)
        }
        _records.value = (_records.value + BodyMeasurement(at, part, valueCm))
            .sortedBy { it.recordedAt }
    }

    /** 每个部位最新一次的数值；没有记录的部位不出现在 Map 里 */
    fun latestByPart(): Map<BodyPart, Float> =
        _records.value
            .groupBy { it.part }
            .mapValues { (_, list) -> list.maxByOrNull { it.recordedAt }!!.valueCm }

    /** 某个部位最近 [days] 天的记录，按时间升序（下一步画折线用） */
    fun seriesOf(part: BodyPart, days: Int): List<BodyMeasurement> {
        val from = LocalDateTime.now().minusDays(days.toLong())
        return _records.value.filter { it.part == part && it.recordedAt.isAfter(from) }
            .sortedBy { it.recordedAt }
    }

    /** 每个部位造 5 个点（-28/-21/-14/-7/0 天），最新值仍是设计稿那组数字 */
    private fun seed(): List<BodyMeasurement> {
        val today = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0)
        return BodyPart.entries.flatMapIndexed { index, part ->
            SEED_DAYS.mapIndexed { i, dayOffset ->
                BodyMeasurement(today.plusDays(dayOffset.toLong()), part, SEED_VALUES[index] + SEED_DELTAS[i])
            }
        }
    }

    private companion object {
        val SEED_VALUES = floatArrayOf(20f, 30f, 20f, 40f, 10f, 15f)
        /** 越早的店略大一点，折线才有「在变细」的走势 */
        val SEED_DELTAS = floatArrayOf(1.5f, 1.0f, 0.5f, 0.2f, 0f)
        val SEED_DAYS = intArrayOf(-28, -21, -14, -7, 0)
    }
}