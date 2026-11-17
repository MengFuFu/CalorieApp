package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.WeightRecord
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 【临时】内存仓库，只为让体重页先跑起来。
 * M4 建好 weight_record 表后，用 RoomWeightRepository 替换掉它，
 * 上层 ViewModel 只依赖它暴露的方法，换实现时不用改 UI。
 *
 * 预置数据取自设计稿：55 → 52，目标 59.9，身高 168（这样 BMI 正好 18.43）。
 */
@Singleton
class TempWeightRepository @Inject constructor() {

    /** 档案字段，M5 会挪到 ProfileRepository */
    val startWeightKg = 55.0f
    val targetWeightKg = 59.9f
    val heightCm = 168.0f
    val bodyFatPercent: Float? = null

    private val records = mutableListOf<WeightRecord>()

    init {
        val today = LocalDate.now()
        records += WeightRecord(today.minusDays(5), 55.0f)
        records += WeightRecord(today.minusDays(3), 52.0f)
        records += WeightRecord(today.minusDays(1), 52.0f)
        records += WeightRecord(today, 52.0f)
    }

    fun all(): List<WeightRecord> = records.sortedBy { it.date }

    /** 同一天重复记录以最后一次为准，避免一天出现两个点 */
    fun add(date: LocalDate, weightKg: Float) {
        records.removeAll { it.date == date }
        records += WeightRecord(date, weightKg)
        records.sortBy { it.date }
    }

    /** 查某一天已经记过的体重，没有返回 null（输入面板预填要用） */
    fun weightOn(date: LocalDate): Float? =
        records.firstOrNull { it.date == date }?.weightKg
}