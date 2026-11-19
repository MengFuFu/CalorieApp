package com.mh.calorie.ui.sport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mh.calorie.data.temp.TempSportRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SportRecordViewModel @Inject constructor(
    private val repo: TempSportRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SportRecordUiState())
    val uiState: StateFlow<SportRecordUiState> = _uiState.asStateFlow()

    private var month: YearMonth = YearMonth.now()

    init {
        // 仓库一变就重算，保存后页面自动刷新
        viewModelScope.launch {
            repo.records.collect { refresh() }
        }
    }

    fun previousMonth() {
        month = month.minusMonths(1)
        refresh()
    }

    fun nextMonth() {
        if (month < YearMonth.now()) {
            month = month.plusMonths(1)
            refresh()
        }
    }

    private fun refresh() {
        val monthRecords = repo.all().filter { YearMonth.from(it.date) == month }

        val groups = monthRecords
            .groupBy { it.date }
            .toSortedMap(reverseOrder())
            .map { (date, list) ->
                SportDayGroup(
                    dateText = date.format(DATE_FORMAT),
                    rows = list.sortedByDescending { it.recordedAt }.map { record ->
                        SportRecordRow(
                            timeText = record.recordedAt.format(TIME_FORMAT),
                            name = record.name,
                            quantityText = formatQuantity(record.quantity) + record.unitLabel,
                            kcalText = String.format(Locale.US, "%.2f", record.kcal)
                        )
                    }
                )
            }

        _uiState.value = SportRecordUiState(
            monthText = month.format(MONTH_FORMAT),
            totalText = String.format(Locale.US, "%.0f", monthRecords.sumOf { it.kcal.toDouble() }),
            groups = groups,
            isEmpty = groups.isEmpty(),
            canGoNext = month < YearMonth.now()
        )
    }

    /** 10.0 → "10"，10.5 → "10.5" */
    private fun formatQuantity(value: Float): String =
        if (value % 1f == 0f) value.toInt().toString()
        else String.format(Locale.US, "%.1f", value)

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
    }

}