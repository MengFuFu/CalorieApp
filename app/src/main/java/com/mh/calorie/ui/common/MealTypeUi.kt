package com.mh.calorie.ui.common

import androidx.annotation.StringRes
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.MealType

/** 餐次 → 文案资源 */
@get:StringRes
val MealType.labelRes: Int
    get() = when(this) {
        MealType.BREAKFAST -> R.string.stats_meal_breakfast
        MealType.LUNCH -> R.string.stats_meal_lunch
        MealType.DINNER -> R.string.stats_meal_dinner
        MealType.SNACK -> R.string.stats_meal_snack
    }

/** 点一下切到下一个餐次（食物库底部那个「早餐 ▾」用） */
fun MealType.next(): MealType = when (this) {
    MealType.BREAKFAST -> MealType.LUNCH
    MealType.LUNCH -> MealType.DINNER
    MealType.DINNER -> MealType.SNACK
    MealType.SNACK -> MealType.BREAKFAST
}