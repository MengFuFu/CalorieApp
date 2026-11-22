package com.mh.calorie.ui.food

/**
 * 一条食物。
 * [kcal] 的含义由所属分类决定：/100g、/100ml 还是「每份」。
 */
data class FoodItem(
    val name: String,
    val kcal: Int,
    /** 第二计量单位：个 / 根 / 颗 / 杯 / 碗…；null 表示只能按重量或份数记 */
    val portionUnit: String? = null,
    /** 一个该单位大约多少克（毫升），用于换算和「≈100g」提示 */
    val gramPerUnit: Float = 0f,
    /** 自定义食物自带单位文案，如 "1g"、"1份"；为空时用分类单位 */
    val unitText: String? = null,
    val favorite: Boolean = false
)

/** 左侧分类。unit 是列表里跟热量后面那个单位文案 */
enum class FoodCategory(val title: String, val unit: String) {

    FREQUENT("我的常用", "/100g"),
    FAVORITE("我的收藏", "/100g"),
    CUSTOM("自定义", "/100g"),
    STAPLE("主食类", "/100g"),
    VEG_FRUIT("蔬菜水果", "/100g"),
    SNACK("零食甜点", "/100g"),
    SOUP("汤粥类", "/100g"),
    MIXED("混合餐食", "/100g"),
    SOY("豆奶制品", "/100g"),
    EGG("蛋类制品", "/100g"),
    MEAT("肉禽水产", "/100g"),
    MILK_TEA("奶茶分区", "/100ml"),
    DRINK("非豆奶饮品", "/100ml"),
    RESTAURANT("餐厅", "/份"),
    MARKET("超市便利", "/份")

}

/** 份量面板的量纲：按克 / 按毫升 / 按份 */
enum class PortionMode { WEIGHT, VOLUME, PORTION}

val FoodCategory.portionMode: PortionMode
    get() = when(this) {
        FoodCategory.MILK_TEA, FoodCategory.DRINK -> PortionMode.VOLUME
        FoodCategory.RESTAURANT, FoodCategory.MARKET -> PortionMode.PORTION
        else -> PortionMode.WEIGHT
    }

/** 主单位文案 */
val PortionMode.baseUnit: String
    get() = when (this) {
        PortionMode.WEIGHT -> "g"
        PortionMode.VOLUME -> "ml"
        PortionMode.PORTION -> "份"
    }

/** 打开面板时的默认值：100g / 100ml / 1份 */
val PortionMode.defaultValue: String
    get() = if (this == PortionMode.PORTION) "1" else "100"