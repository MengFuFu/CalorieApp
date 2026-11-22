package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.SportDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** 运动草稿（还没点「完成」的那批），M4 之后可以换成 SavedStateHandle 或 Room 暂存表 */
@Singleton
class TempSportDraftRepository @Inject constructor() {

    private val _drafts = MutableStateFlow<List<SportDraft>>(emptyList())
    val drafts: StateFlow<List<SportDraft>> = _drafts.asStateFlow()

    private var nextId = 1L

    /** 返回新草稿的 id，方便列表删除 */
    fun add(
        name: String,
        quantity: Float,
        unitLabel: String,
        kcal: Float,
        recordedAt: LocalDateTime
    ) : Long {
        val id = nextId++
        _drafts.value = _drafts.value +
                SportDraft(id, name, quantity, unitLabel, kcal, recordedAt)
        return id
    }

    fun remove(id: Long) {
        _drafts.value = _drafts.value.filterNot { it.id == id}
    }

    fun clear() {
        _drafts.value = emptyList()
    }

    fun totalKcal(): Float = _drafts.value.sumOf { it.kcal.toDouble() }.toFloat()
}