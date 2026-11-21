package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.FoodLog
import com.mh.calorie.core.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.ls.LSInput
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** 临时的食物日记仓库（M4 接 Room 后整类删除） */
@Singleton
class TempFoodLogRepository @Inject constructor() {

    private val _logs = MutableStateFlow<List<FoodLog>>(emptyList())
    val logs: StateFlow<List<FoodLog>> = _logs.asStateFlow()

    fun add(log: FoodLog) {
        _logs.value = _logs.value + log
    }

    fun ofDate(date: LocalDate): List<FoodLog> = _logs.value.filter { it.date == date }

    fun ofMeal(date: LocalDate, meal: MealType): List<FoodLog> =
        ofDate(date).filter { it.meal == meal }
}