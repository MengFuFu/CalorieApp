package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.CustomFood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** 自定义食物仓库（M4 接 Room 后整类删除） */
@Singleton
class TempCustomFoodRepository @Inject constructor() {

    private val _items = MutableStateFlow<List<CustomFood>>(emptyList())
    val items: StateFlow<List<CustomFood>> = _items.asStateFlow()

    /** 同名覆盖，避免重复录入 */
    fun add(food: CustomFood) = _items.update { list ->
        list.filterNot { it.name == food.name } + food
    }

    fun toggleFavorite(name: String) = _items.update { list ->
        list.map { if (it.name == name) it.copy(favorite = !it.favorite) else it }
    }

    fun byName(name: String): CustomFood? = _items.value.firstOrNull { it.name == name }
}