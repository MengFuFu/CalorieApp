package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.CustomSport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 自定义运动临时仓库（M4 接 Room 后整类删除） */
@Singleton
class TempCustomSportRepository @Inject constructor() {

    private val _items = MutableStateFlow<List<CustomSport>>(emptyList())
    val items: StateFlow<List<CustomSport>> = _items.asStateFlow()

    fun all(): List<CustomSport> = _items.value

    fun add(sport: CustomSport) {
        // 同名的直接覆盖，避免「跑步」出现两条
        _items.value = _items.value.filterNot { it.name == sport.name } + sport
    }
}