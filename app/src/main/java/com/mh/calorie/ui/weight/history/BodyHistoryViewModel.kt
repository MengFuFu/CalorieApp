package com.mh.calorie.ui.weight.history

import androidx.lifecycle.ViewModel
import com.mh.calorie.data.temp.TempBodyMeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BodyHistoryViewModel @Inject constructor(
    private val repo: TempBodyMeasurementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyHistoryUiState())
    val uiState: StateFlow<BodyHistoryUiState> = _uiState.asStateFlow()

    private var month: YearMonth = YearMonth.now()

    init {
        refresh()
    }

    fun previousMonth() {
        month = month.minusMonths(1)
        refresh()
    }

    fun nextMonth() {
        // 到当前月就停，避免翻到「未来的空月份」
        if(month >= YearMonth.now()) return
        month = month.plusMonths(1)
        refresh()
    }

    private fun refresh() {
        val groups = repo.records.value
            .filter { YearMonth.from(it.date) == month }
            .groupBy { it.date }
            .toSortedMap(reverseOrder()) // 日期倒序：今天在最上面
            .map { (date, list) ->
                BodyDayGroup(
                    dateText = date.format(DAY_FORMAT),
                    rows = list.sortedByDescending { it.recordedAt }.map {
                        BodyHistoryRow(
                            time = it.recordedAt.format(TIME_FORMAT),
                            part = it.part,
                            valueCm = it.valueCm
                        )
                    }
                )
            }

        _uiState.value = BodyHistoryUiState(
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