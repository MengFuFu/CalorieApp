package com.mh.calorie.core.domain.bmi

/**
 * 目标进度。
 *
 * 必须 clamp 到 0..100：增重任务里「当前 52 < 初始 55」会算出负数，
 * 减重任务里「越减越反弹」会算出超过 100，两者都不能直接把裸值丢给进度条。
 */
object WeightProgressCalculator {

    fun progressPercent(startKg: Float, currentKg: Float, targetKg: Float): Int {
        val total = targetKg - startKg
        if(total == 0f) return 100
        val done = (currentKg - startKg) / total
        return (done * 100f).coerceIn(0f, 100f).toInt()
    }

    /** 累计变化：正数是增重，负数是减重，由调用方决定显示成 +1.2 / -3 */
    fun changedKg(startKg: Float, currentKg: Float): Float = currentKg - startKg

}