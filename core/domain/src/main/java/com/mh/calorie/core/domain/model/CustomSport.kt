package com.mh.calorie.core.domain.model

/**
 * 用户自定义的运动。
 * [minutes] 与 [kcal] 是用户填的那一组「基准」——列表里就照这个显示（200 kcal/30分钟），
 * 换算成别的时长时按 `kcal * 数量 / minutes` 线性放大。
 */
data class CustomSport (
    val name: String,
    val minutes: Int,
    val kcal: Float,
    /** 照片本地 Uri 字符串，可为空 */
    val photoUri: String? = null
)