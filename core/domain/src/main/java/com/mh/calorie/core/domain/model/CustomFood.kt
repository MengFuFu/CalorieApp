package com.mh.calorie.core.domain.model

/** 用户自己录入的食物。热量按 [qty] + [unit] 这一份计，不是 per 100g */
data class CustomFood(
    val name: String,
    val qty: Float,
    val unit: String,
    val kcal: Float,
    val protein: Float = 0f, // 可选：这一份里的克数
    val fat: Float = 0f,
    val carb: Float = 0f,
    val photoUri: String? = null,
    val favorite: Boolean = false
) {
    /** 三大宏量是否都填了，没填就用分类估算 */
    val hasMacro: Boolean get() = protein > 0f || fat > 0f || carb > 0f
}