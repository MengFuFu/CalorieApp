package com.mh.calorie.ui.weight.body

import androidx.lifecycle.ViewModel
import com.mh.calorie.core.domain.model.BodyPart
import com.mh.calorie.data.temp.TempBodyMeasurementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.enums.EnumEntries

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

    /** 面板一次提交多项；没填的部位不会出现在 Map 里 */
    fun save(entries: Map<BodyPart, Float>, at: LocalDateTime) {
        if(entries.isEmpty()) return
        entries.forEach { part, valueCm -> repo.add(at, part, valueCm)  }
        refresh()  // 立刻刷新网格，页面的 StateFlow 会收到新值
    }

    /** 重新读仓库，把六个部位的最新值铺进网格 */
    fun refresh() {
        val latest = repo.latestByPart()
        _uiState.update { state ->
            state.copy(rows = BodyPart.entries.map { part -> BodyPartRow(part, latest[part]) })
        }
    }

}