package com.mh.calorie.ui.food

/**
 * 临时：食物库现在只有热量，没有蛋白/脂肪/碳水。
 * 这里按分类的典型能量占比把热量拆成三大宏量，保证统计页能显示；
 * 接真实营养成分库后**整个文件删除**，改成读食物自带的宏量字段。
 */
object MacroEstimator {

    /** 能量占比：碳水 / 蛋白 / 脂肪 */
    private fun ratios(category: FoodCategory): Triple<Float, Float, Float> = when (category) {
        FoodCategory.STAPLE -> Triple(0.76f, 0.12f, 0.12f)
        FoodCategory.VEG_FRUIT -> Triple(0.85f, 0.08f, 0.07f)
        FoodCategory.SNACK -> Triple(0.55f, 0.06f, 0.39f)
        FoodCategory.SOUP -> Triple(0.45f, 0.22f, 0.33f)
        FoodCategory.MIXED -> Triple(0.45f, 0.20f, 0.35f)
        FoodCategory.SOY -> Triple(0.28f, 0.34f, 0.38f)
        FoodCategory.EGG -> Triple(0.08f, 0.36f, 0.56f)
        FoodCategory.MEAT -> Triple(0.04f, 0.40f, 0.56f)
        FoodCategory.MILK_TEA -> Triple(0.62f, 0.08f, 0.30f)
        FoodCategory.DRINK -> Triple(0.90f, 0.05f, 0.05f)
        FoodCategory.RESTAURANT, FoodCategory.MARKET -> Triple(0.48f, 0.18f, 0.34f)
        FoodCategory.FREQUENT, FoodCategory.FAVORITE, FoodCategory.CUSTOM ->
            Triple(0.50f, 0.20f, 0.30f)
    }

    /** 返回 (碳水克, 蛋白克, 脂肪克) */
    fun split(category: FoodCategory, kcal: Float): Triple<Float, Float, Float> {
        val (c, p, f) = ratios(category)
        return Triple(kcal * c / 4f, kcal * p / 4f, kcal * f / 9f)
    }

}