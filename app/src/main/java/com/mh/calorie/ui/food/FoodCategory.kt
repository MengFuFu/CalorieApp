package com.mh.calorie.ui.food

/** 一条食物：名称 + 每单位热量（单位由所属分类决定） */
data class FoodItem(
    val name: String,
    val kcal: Int
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