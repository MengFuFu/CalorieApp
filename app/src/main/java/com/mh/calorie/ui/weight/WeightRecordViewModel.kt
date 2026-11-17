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

    fun addWeight(weightKg: Float) {
        repo.add(LocalDate.now(), weightKg)
        refresh(_uiState.value.rangeDays)
    }

    private fun refresh(rangeDays: Int) {
        val all = repo.all()
        val current = all.lastOrNull()?.weightKg ?: repo.startWeightKg
        val bmi = BmiCalculator.bmi(current, repo.heightCm)
        val from = LocalDate.now().minusDays(rangeDays.toLong() - 1)

        val points = all.filter { it.date >= from }.map {
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
            lastRecordDate = all.lastOrNull()?.date,
            rangeDays = rangeDays
        )
    }

    private companion object {
        const val DEFAULT_RANGE_DAYS = 30
        val MD_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d", Locale.CHINA)
    }
}