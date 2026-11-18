package com.mh.calorie.ui.weight

import androidx.lifecycle.ViewModel
import com.mh.calorie.data.temp.TempWeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeightHistoryViewModel @Inject constructor(
    private val repo: TempWeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightHistoryUiState())
    val uiState: StateFlow<WeightHistoryUiState> = _uiState.asStateFlow()

    /** 当前看的月份，默认本月 */
    private var month: YearMonth = YearMonth.now()

    init {
        refresh()
    }

    fun previousMonth() {
        month = month.minusMonths(1)
        refresh()
    }

    fun nextMonth() {
        if (month >= YearMonth.now()) return
        month = month.plusMonths(1)
        refresh()
    }

    private fun refresh() {
        val groups = repo.all()
            .filter { YearMonth.from(it.date) == month }
            .groupBy { it.date }
            .toSortedMap(reverseOrder())
            .map { (date, list) ->
                WeightDayGroup(
                    dateText = date.format(DAY_FORMAT),
                    rows = list.sortedByDescending { it.recordedAt }.map {
                        WeightHistoryRow(
                            time = it.recordedAt.format(TIME_FORMAT),
                            weightKg = it.weightKg,
                            bodyFatPercent = repo.bodyFatPercent
                        )
                    }
                )
            }

        _uiState.value = WeightHistoryUiState(
            monthText = month.format(MONTH_FORMAT),
            groups = groups,
            isEmpty = groups.isEmpty(),
            canGoNext = month < YearMonth.now()
        )
    }

    private companion object {
        val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
        val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
    }
}