package com.mh.calorie.core.domain.bmi

/** BMI 分级：用中国成人标准，和首页、M5 档案、统计页共用同一套阈值，不许各页自己写数字 */
enum class BmiCategory(val label: String) {

    LOW("偏低"),
    NORMAL("正常"),
    OVERWEIGHT("超重"),
    OBESE("肥胖")

}

object BmiCalculator {

    /** 分级阈值（中国标准：<18.5 偏低 / 18.5~23.9 正常 / 24~27.9 超重 / ≥28 肥胖） */
    const val LOW_MAX = 18.5f
    const val NORMAL_MAX = 24.0f
    const val OVERWEIGHT_MAX = 28.0f

    /** 色卡量程：给 BmiScaleView 用，低于 15 或高于 32 的人也会被夹在两端显示 */
    const val SCALE_MIN = 15.0f
    const val SCALE_MAX = 32.0f

    fun bmi(weightKg: Float, heightCm: Float): Float {
        if (heightCm <= 0f) return 0f
        val m = heightCm / 100f
        return weightKg / (m * m)
    }

    fun category(bmi: Float): BmiCategory = when {
        bmi < LOW_MAX -> BmiCategory.LOW
        bmi < NORMAL_MAX -> BmiCategory.NORMAL
        bmi < OVERWEIGHT_MAX -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }
}