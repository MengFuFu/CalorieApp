package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.WeightRecord
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 【临时】内存仓库，只为让体重页先跑起来。
 * M4 建好 weight_record 表后，用 RoomWeightRepository 替换掉它。
 */
@Singleton
class TempWeightRepository @Inject constructor() {

    /** 档案字段，M5 会挪到 ProfileRepository */
    var startWeightKg = 55.0f
    var targetWeightKg = 59.9f
    val heightCm = 168.0f
    val bodyFatPercent: Float? = null

    private val records = mutableListOf<WeightRecord>()

    init {
        // 演示数据带上时刻，历史列表才有 HH:mm 可显示
        val now = LocalDateTime.now().withSecond(0).withNano(0)
        records += WeightRecord(now.minusDays(5).withHour(7).withMinute(20), 55.0f)
        records += WeightRecord(now.minusDays(3).withHour(8).withMinute(5), 52.0f)
        records += WeightRecord(now.minusDays(1).withHour(22).withMinute(28), 52.0f)
        records += WeightRecord(now.withHour(9).withMinute(12), 51.0f)
    }

    /** 全部记录，按时间升序；同一天可以有多条 */
    fun all(): List<WeightRecord> = records.sortedBy { it.recordedAt }

    /** 追加一条，不再覆盖同一天的历史 */
    fun add(recordedAt: LocalDateTime, weightKg: Float) {
        // 面板只能选到「分」，同一分钟内连续记录会得到完全相同的 recordedAt；
        // 而「当天最新」是用 maxByOrNull 取的，并列时返回最早那条，
        // 会导致同一分钟内的后几次记录永远不生效。这里把时间往后顶到不重复为止。
        var at = recordedAt
        while (records.any {it.recordedAt == at}) {
            at = at.plusSeconds(1)
        }
        records += WeightRecord(at, weightKg)
    }

    /** 某一天最后一次的体重（输入面板预填要用） */
    fun weightOn(date: LocalDate): Float? =
        records.filter { it.date == date }.maxByOrNull { it.recordedAt }?.weightKg
}