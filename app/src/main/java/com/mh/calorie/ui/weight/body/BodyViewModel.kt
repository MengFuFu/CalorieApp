package com.mh.calorie.ui.weight.body

import androidx.lifecycle.ViewModel
import com.mh.calorie.core.domain.model.BodyPart
import com.mh.calorie.data.temp.TempBodyMeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BodyViewModel @Inject constructor(
    private val repo: TempBodyMeasurementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyUiState())
    val uiState: StateFlow<BodyUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectPart(part: BodyPart) {
        _uiState.update { it.copy(selectedPart = part) }
    }

    fun setRange(days: Int) {
        _uiState.update { it.copy(rangeDays = days) }
    }

    /** 重新读仓库，把六个部位的最新值铺进网格 */
    fun refresh() {
        val latest = repo.latestByPart()
        _uiState.update { state ->
            state.copy(rows = BodyPart.entries.map { part -> BodyPartRow(part, latest[part]) })
        }
    }

}