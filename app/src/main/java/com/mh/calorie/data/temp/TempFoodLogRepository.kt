package com.mh.calorie.data.temp

import com.mh.calorie.core.domain.model.FoodLog
import com.mh.calorie.core.domain.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** 临时的食物日记仓库（M4 接 Room 后整类删除） */
@Singleton
class TempFoodLogRepository @Inject constructor() {

    private val _logs = MutableStateFlow<List<FoodLog>>(emptyList())
    val logs: StateFlow<List<FoodLog>> = _logs.asStateFlow()

    private var nextId = 1L

    fun ofDate(date: LocalDate): List<FoodLog> = _logs.value.filter { it.date == date }

    fun ofMeal(date: LocalDate, meal: MealType): List<FoodLog> =
        ofDate(date).filter { it.meal == meal }

    /** 同名同餐次就 +1 份，否则新建一条 */
    fun addOrIncrease(
        date: LocalDate,
        meal: MealType,
        name: String,
        baseQty: Float,
        unit: String,
        baseKcal: Float,
        carb: Float,
        protein: Float,
        fat: Float
    ) {
        val list = _logs.value
        val existing = list.firstOrNull { it.date == date && it.meal == meal && it.name == name }
        _logs.value = if (existing != null) {
            list.map { if (it.id == existing.id) it.copy(count = it.count + 1) else it }
        } else {
            list + FoodLog(
                id = nextId++,
                recordedAt = LocalDateTime.now(),
                meal = meal,
                name = name,
                baseQty = baseQty,
                unit = unit,
                baseKcal = baseKcal,
                baseCarb = carb,
                baseProtein = protein,
                baseFat = fat
            )
        }
    }

    fun increase(id: Long) {
        _logs.value = _logs.value.map { if (it.id == id) it.copy(count = it.count + 1) else it }
    }

    /** 减到 0 就整条删掉 */
    fun decrease(id: Long) {
        _logs.value = _logs.value.mapNotNull {
            when {
                it.id != id -> it
                it.count <= 1 -> null
                else -> it.copy(count = it.count - 1)
            }
        }
    }

    fun countOf(date: LocalDate, meal: MealType, name: String): Int =
        ofMeal(date, meal).firstOrNull { it.name == name }?.count ?: 0

    fun idOf(date: LocalDate, meal: MealType, name: String): Long? =
        ofMeal(date, meal).firstOrNull { it.name == name }?.id

    /** 当前餐次一共几份 */
    fun totalCount(date: LocalDate, meal: MealType): Int =
        ofMeal(date, meal).sumOf { it.count }
}