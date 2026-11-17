package com.mh.calorie.ui.weight

import com.mh.calorie.core.domain.bmi.BmiCategory
import java.time.LocalDate

/** 一个绘制点：label 形如 "11/13"，date 留着给点选交互用 */
data class WeightChartPoint(
    val label: String,
    val date: LocalDate,
    val weightKg: Float
)

/**
 * 体重页的完整渲染状态。
 * 页面写的时候只认这个类，不许在 Fragment 里自己算 BMI / 进度，
 * 这样 M4/M5 换成真数据时，UI 一行都不用动。
 */
data class WeightUiState(
    val currentWeightKg: Float = 0f,
    val startWeightKg: Float = 0f,
    val targetWeightKg: Float = 0f,
    val heightCm: Float = 0f,
    val bmi: Float = 0f,
    val bmiCategory: BmiCategory = BmiCategory.NORMAL,
    val bodyFatPercent: Float? = null,
    val changedKg: Float = 0f,
    val progressPercent: Int = 0,
    /** 目标体重 > 初始体重 = 增重任务，进度文案跟着变 */
    val isGainGoal: Boolean = false,
    val chartPoints: List<WeightChartPoint> = emptyList(),
    /** 日期(yyyy-MM-dd) → 当天体重。输入面板切日期时回填用，避免用户在历史日期上误改 */
    val weightByDate: Map<String, Float> = emptyMap(),
    val lastRecordDate: LocalDate? = null,
    val rangeDays: Int = 30
)