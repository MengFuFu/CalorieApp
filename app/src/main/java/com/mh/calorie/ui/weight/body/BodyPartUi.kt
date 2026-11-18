package com.mh.calorie.ui.weight.body

import com.mh.calorie.R
import com.mh.calorie.core.domain.model.BodyPart

/** 部位 → 文案。网格与面板共用，避免两处各写一份 when */
fun BodyPart.labelRes(): Int = when (this) {
    BodyPart.WAIST -> R.string.body_waist
    BodyPart.CHEST -> R.string.body_chest
    BodyPart.HIP -> R.string.body_hip
    BodyPart.THIGH -> R.string.body_thigh
    BodyPart.CALF -> R.string.body_calf
    BodyPart.ARM -> R.string.body_arm
}

/** 部位 → 图标 */
fun BodyPart.iconRes(): Int = when (this) {
    BodyPart.WAIST -> R.drawable.ic_body_waist
    BodyPart.CHEST -> R.drawable.ic_body_chest
    BodyPart.HIP -> R.drawable.ic_body_hip
    BodyPart.THIGH -> R.drawable.ic_body_thigh
    BodyPart.CALF -> R.drawable.ic_body_calf
    BodyPart.ARM -> R.drawable.ic_body_arm
}