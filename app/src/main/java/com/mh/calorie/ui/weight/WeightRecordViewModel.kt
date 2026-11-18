package com.mh.calorie.ui.weight

import androidx.lifecycle.ViewModel
import com.mh.calorie.core.domain.bmi.BmiCalculator
import com.mh.calorie.core.domain.bmi.WeightProgressCalculator
import com.mh.calorie.data.temp.TempWeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeightRecordViewModel @Inject constructor(
    private val repo: TempWeightRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        refresh(DEFAULT_RANGE_DAYS)
    }

    /** 近一月 / 近一周切换 */
    fun selectRange(days: Int) = refresh(days)

    /** 记录体重。at 由输入面板的日期滚轮决定，不传就是今天 */
    fun addWeight(weightKg: Float, at: LocalDateTime = LocalDateTime.now()) {
        // 保留每一次打卡，历史列表要显示同一天多条；图表在 refresh 里按天去重
        repo.add(at, weightKg)
        refresh(_uiState.value.rangeDays)
    }

    /** 改初始体重：进度基准变了，整页重算 */
    fun setStartWeight(startKg: Float) {
        repo.startWeightKg = startKg
        refresh(_uiState.value.rangeDays)
    }

    /** 改目标体重：进度终点和「增重/减重」文案一起变 */
    fun setTargetWeight(targetKg: Float) {
        repo.targetWeightKg = targetKg
        refresh(_uiState.value.rangeDays)
    }

    private fun refresh(rangeDays: Int) {
        val all = repo.all()
        // 同一天记了多次时，图表只取当天最后一次
        val daily = all.groupBy { it.date }
            .mapValues { (_, list) -> list.maxByOrNull { it.recordedAt }!! }
            .toSortedMap()

        val current = daily.values.lastOrNull()?.weightKg ?: repo.startWeightKg
        val bmi = BmiCalculator.bmi(current, repo.heightCm)
        val from = LocalDate.now().minusDays(rangeDays.toLong() - 1)

        val points = daily.values.filter { it.date >= from }.map {
            WeightChartPoint(label = it.date.format(MD_FORMAT), date = it.date, weightKg = it.weightKg)
        }

        _uiState.value = WeightUiState(
            currentWeightKg = current,
            startWeightKg = repo.startWeightKg,
            targetWeightKg = repo.targetWeightKg,
            heightCm = repo.heightCm,
            bmi = bmi,
            bmiCategory = BmiCalculator.category(bmi),
            bodyFatPercent = repo.bodyFatPercent,
            changedKg = WeightProgressCalculator.changedKg(repo.startWeightKg, current),
            progressPercent = WeightProgressCalculator.progressPercent(repo.startWeightKg, current, repo.targetWeightKg),
            isGainGoal = repo.targetWeightKg > repo.startWeightKg,
            chartPoints = points,
            lastRecordDate = daily.keys.lastOrNull(),
            // 输入面板按 "yyyy-MM-dd" 查历史值，所以 key 要转成字符串
            weightByDate = daily.entries.associate { it.key.toString() to it.value.weightKg },
            rangeDays = rangeDays,
            updateTick = _uiState.value.updateTick + 1
        )
    }

    private companion object {
        const val DEFAULT_RANGE_DAYS = 30
        val MD_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d", Locale.CHINA)
    }
}