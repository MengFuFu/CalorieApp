package com.mh.calorie.ui.sport

import androidx.annotation.StringRes
import com.mh.calorie.R

/** 计量单位：min 永远有；其余四个按运动类型分配 */
enum class SportUnit(@StringRes val labelRes: Int) {
    MIN(R.string.sport_unit_min),
    KM(R.string.sport_unit_km),
    ROUND(R.string.sport_unit_round),
    SESSION(R.string.sport_unit_session),
    REP(R.string.sport_unit_rep)
}

/** 运动库左侧分类，声明顺序即显示顺序 */
enum class SportCategory(@StringRes val labelRes: Int) {
    FAVORITE(R.string.sport_cat_favorite),
    CUSTOM(R.string.sport_cat_custom),
    RUN(R.string.sport_cat_run),
    OUTDOOR(R.string.sport_cat_outdoor),
    CYCLING(R.string.sport_cat_cycling),
    WATER(R.string.sport_cat_water),
    BALL(R.string.sport_cat_ball),
    DANCE(R.string.sport_cat_dance),
    MIND(R.string.sport_cat_mind),
    STRENGTH(R.string.sport_cat_strength)
}

/**
 * 一条运动。
 * [secondUnit] 是除 min 之外的第二计量单位；
 * [secondUnitsPer10Min] 是「10 分钟里有几个该单位」，
 * 例如 跑步 12km/h → 2.0（10 分钟跑 2 公里）、篮球 20 分钟一局 → 0.5、俯卧撑 20 个/分钟 → 200。
 * 有了它，kcal 换算只需要一个公式：kcalPer10Min × 数量 ÷ secondUnitsPer10Min。
 */
data class SportItem(
    val category: SportCategory,
    val name: String,
    val kcalPer10Min: Float,
    val secondUnit: SportUnit = SportUnit.SESSION,
    val secondUnitsPer10Min: Float = 0f
) {
    /** 把「数量 + 单位」换算成千卡 */
    fun kcalFor(value: Float, unit: SportUnit): Float = when {
        unit == SportUnit.MIN -> kcalPer10Min * value / 10f
        secondUnitsPer10Min <= 0f -> 0f
        else -> kcalPer10Min * value / secondUnitsPer10Min
    }
}

/**
 * 静态运动库（UI 阶段数据，来源：设计稿运动库清单）。
 * 名称暂时写死中文：接 Room / 服务端时整体换成实体表，届时再迁到 strings。
 */
object SportCatalog {

    /** 原始清单：只有时间维度，距离维度在下面统一补 */
    private val rawItems: List<SportItem> = buildList {
        // ── 跑步健走 ──
        add(SportItem(SportCategory.RUN, "跑步（快速）", 90f))
        add(SportItem(SportCategory.RUN, "跑步（中速）", 78f))
        add(SportItem(SportCategory.RUN, "跑步（慢速）", 66f))
        add(SportItem(SportCategory.RUN, "马拉松", 57f))
        add(SportItem(SportCategory.RUN, "走路", 27f))
        add(SportItem(SportCategory.RUN, "散步", 27f))
        add(SportItem(SportCategory.RUN, "步行", 27f))
        add(SportItem(SportCategory.RUN, "徒步快走", 58f))
        add(SportItem(SportCategory.RUN, "爬坡", 83.3f))

        // ── 球类竞技 ──
        add(SportItem(SportCategory.BALL, "篮球", 72f))
        add(SportItem(SportCategory.BALL, "篮球（非比赛）", 66f))
        add(SportItem(SportCategory.BALL, "篮球（投篮）", 50f))
        add(SportItem(SportCategory.BALL, "足球", 67f))
        add(SportItem(SportCategory.BALL, "排球", 55f))
        add(SportItem(SportCategory.BALL, "网球", 67f))
        add(SportItem(SportCategory.BALL, "羽毛球", 53f))
        add(SportItem(SportCategory.BALL, "乒乓球", 39f))
        add(SportItem(SportCategory.BALL, "高尔夫", 43f))
        add(SportItem(SportCategory.BALL, "棒球", 39f))
        add(SportItem(SportCategory.BALL, "垒球", 39f))
        add(SportItem(SportCategory.BALL, "手球", 116f))
        add(SportItem(SportCategory.BALL, "壁球", 67f))
        add(SportItem(SportCategory.BALL, "台球", 24f))
        add(SportItem(SportCategory.BALL, "保龄球", 29f))
        add(SportItem(SportCategory.BALL, "橄榄球", 61f))
        add(SportItem(SportCategory.BALL, "曲棍球", 65f))
        add(SportItem(SportCategory.BALL, "板球", 61.3f))
        add(SportItem(SportCategory.BALL, "篮网球", 73.5f))
        add(SportItem(SportCategory.BALL, "福乐球", 73.5f))
        add(SportItem(SportCategory.BALL, "美式足球", 98f))
        add(SportItem(SportCategory.BALL, "长曲棍球", 98f))
        add(SportItem(SportCategory.BALL, "冰上曲棍球", 98f))
        add(SportItem(SportCategory.BALL, "板网球", 98f))
        add(SportItem(SportCategory.BALL, "短柄墙球", 122.5f))
        add(SportItem(SportCategory.BALL, "拳击", 147f))

        // ── 户外探索 ──
        add(SportItem(SportCategory.OUTDOOR, "登山", 72f))
        add(SportItem(SportCategory.OUTDOOR, "攀岩", 91f))
        add(SportItem(SportCategory.OUTDOOR, "滑雪", 67f))
        add(SportItem(SportCategory.OUTDOOR, "滑冰", 67f))
        add(SportItem(SportCategory.OUTDOOR, "轮滑", 60f))
        add(SportItem(SportCategory.OUTDOOR, "滑板", 48f))
        add(SportItem(SportCategory.OUTDOOR, "帆船", 36.7f))
        add(SportItem(SportCategory.OUTDOOR, "钓鱼", 36.7f))
        add(SportItem(SportCategory.OUTDOOR, "骑马", 49f))
        add(SportItem(SportCategory.OUTDOOR, "射箭", 49f))
        add(SportItem(SportCategory.OUTDOOR, "爬山", 98f))
        add(SportItem(SportCategory.OUTDOOR, "耐力运动", 98f))
        add(SportItem(SportCategory.OUTDOOR, "赛艇", 98f))
        add(SportItem(SportCategory.OUTDOOR, "山地自行车", 98f))
        add(SportItem(SportCategory.OUTDOOR, "滑旱冰", 122.5f))
        add(SportItem(SportCategory.OUTDOOR, "越野滑雪", 147f))

        // ── 户外骑行 ──
        add(SportItem(SportCategory.CYCLING, "骑行", 65f))
        add(SportItem(SportCategory.CYCLING, "骑共享单车", 77f))
        add(SportItem(SportCategory.CYCLING, "骑山地车", 82f))
        add(SportItem(SportCategory.CYCLING, "户外骑行", 82f))

        // ── 水上运动 ──
        add(SportItem(SportCategory.WATER, "游泳", 63f))
        add(SportItem(SportCategory.WATER, "自由泳", 72f))
        add(SportItem(SportCategory.WATER, "蛙泳", 96f))
        add(SportItem(SportCategory.WATER, "仰泳", 67f))
        add(SportItem(SportCategory.WATER, "蝶泳", 106f))
        add(SportItem(SportCategory.WATER, "潜水", 67f))
        add(SportItem(SportCategory.WATER, "冲浪", 58f))
        add(SportItem(SportCategory.WATER, "划船", 35f))
        add(SportItem(SportCategory.WATER, "快速狗刨游泳", 91.7f))
        add(SportItem(SportCategory.WATER, "慢速狗刨游泳", 66.7f))
        add(SportItem(SportCategory.WATER, "花样游泳", 66.7f))
        add(SportItem(SportCategory.WATER, "快速自由式游泳", 83.3f))
        add(SportItem(SportCategory.WATER, "慢速自由式游泳", 58.3f))
        add(SportItem(SportCategory.WATER, "踩水", 33.3f))
        add(SportItem(SportCategory.WATER, "水中行走", 20.8f))
        add(SportItem(SportCategory.WATER, "水上自行车", 49f))
        add(SportItem(SportCategory.WATER, "皮划艇", 61.3f))
        add(SportItem(SportCategory.WATER, "水肺潜水", 73.5f))

        // ── 有氧舞蹈 ──
        add(SportItem(SportCategory.DANCE, "健身操", 60f))
        add(SportItem(SportCategory.DANCE, "尊巴舞", 58f))
        add(SportItem(SportCategory.DANCE, "搏击操", 100f))
        add(SportItem(SportCategory.DANCE, "开合跳", 68f))
        add(SportItem(SportCategory.DANCE, "高抬腿", 88f))
        add(SportItem(SportCategory.DANCE, "舞蹈", 58f))
        add(SportItem(SportCategory.DANCE, "芭蕾", 46f))
        add(SportItem(SportCategory.DANCE, "街舞", 67f))
        add(SportItem(SportCategory.DANCE, "现代舞", 46f))
        add(SportItem(SportCategory.DANCE, "传统印第安舞", 45.8f))
        add(SportItem(SportCategory.DANCE, "快速舞蹈", 45.8f))
        add(SportItem(SportCategory.DANCE, "扭摆舞", 40f))
        add(SportItem(SportCategory.DANCE, "爵士舞", 40f))
        add(SportItem(SportCategory.DANCE, "踢踏舞", 40f))
        add(SportItem(SportCategory.DANCE, "吉特巴舞", 40f))
        add(SportItem(SportCategory.DANCE, "希腊舞", 37.5f))
        add(SportItem(SportCategory.DANCE, "佛拉明哥舞", 37.5f))
        add(SportItem(SportCategory.DANCE, "摇摆舞", 37.5f))
        add(SportItem(SportCategory.DANCE, "爱尔兰舞", 37.5f))
        add(SportItem(SportCategory.DANCE, "波尔卡", 37.5f))
        add(SportItem(SportCategory.DANCE, "草裙舞", 37.5f))
        add(SportItem(SportCategory.DANCE, "跳舞", 37.5f))
        add(SportItem(SportCategory.DANCE, "慢速舞蹈", 25f))
        add(SportItem(SportCategory.DANCE, "有氧舞蹈", 54.2f))
        add(SportItem(SportCategory.DANCE, "广场舞", 50f))
        add(SportItem(SportCategory.DANCE, "跳操", 58.3f))
        add(SportItem(SportCategory.DANCE, "减肥操", 50f))
        add(SportItem(SportCategory.DANCE, "健美操", 33.3f))
        add(SportItem(SportCategory.DANCE, "方块舞", 37.5f))

        // ── 身心修养 ──
        add(SportItem(SportCategory.MIND, "瑜伽", 29f))
        add(SportItem(SportCategory.MIND, "普拉提", 29f))
        add(SportItem(SportCategory.MIND, "太极", 39f))
        add(SportItem(SportCategory.MIND, "拉伸训练", 29f))
        add(SportItem(SportCategory.MIND, "平衡训练", 29f))
        add(SportItem(SportCategory.MIND, "彻底拉伸", 29f))
        add(SportItem(SportCategory.MIND, "泡沫轴放松", 29f))
        add(SportItem(SportCategory.MIND, "力量瑜伽", 33.3f))
        add(SportItem(SportCategory.MIND, "睡前舒缓", 25f))
        add(SportItem(SportCategory.MIND, "产后瑜伽", 25f))
        add(SportItem(SportCategory.MIND, "空中瑜伽", 41.7f))
        add(SportItem(SportCategory.MIND, "伸展运动", 20.8f))
        add(SportItem(SportCategory.MIND, "高温瑜伽", 49f))

        // ── 肌力训练 ──（清单里「空中蹬车」没给数值，先不收录）
        add(SportItem(SportCategory.STRENGTH, "壶铃", 98f))
        add(SportItem(SportCategory.STRENGTH, "壶铃甩", 112f))
        add(SportItem(SportCategory.STRENGTH, "臂力棒", 37.5f))
        add(SportItem(SportCategory.STRENGTH, "呼啦圈", 36f))
        add(SportItem(SportCategory.STRENGTH, "健美", 98f))
        add(SportItem(SportCategory.STRENGTH, "TRX悬挂带俯身登山", 66.7f))
        add(SportItem(SportCategory.STRENGTH, "举重", 66.7f))
        add(SportItem(SportCategory.STRENGTH, "弹力带臀桥", 50f))
        add(SportItem(SportCategory.STRENGTH, "哑铃深蹲", 50f))
        add(SportItem(SportCategory.STRENGTH, "平板卧推", 50f))
        add(SportItem(SportCategory.STRENGTH, "靠墙俯卧撑", 45.8f))
        add(SportItem(SportCategory.STRENGTH, "哑铃平底飞鸟", 41.7f))
        add(SportItem(SportCategory.STRENGTH, "举重锻炼", 73.5f))
        add(SportItem(SportCategory.STRENGTH, "肩颈拉伸", 16f))
        add(SportItem(SportCategory.STRENGTH, "腹部拉伸", 27f))
        add(SportItem(SportCategory.STRENGTH, "平板旋转", 33f))
        add(SportItem(SportCategory.STRENGTH, "剪刀腿", 31.7f))
        add(SportItem(SportCategory.STRENGTH, "爬楼", 130f))
        add(SportItem(SportCategory.STRENGTH, "杠铃卧推", 66.7f))
        add(SportItem(SportCategory.STRENGTH, "站姿杠铃推举", 66.7f))
        add(SportItem(SportCategory.STRENGTH, "哑铃肩推", 58f))
        add(SportItem(SportCategory.STRENGTH, "全身循环", 100f))
        add(SportItem(SportCategory.STRENGTH, "全身拉伸", 36f))
        add(SportItem(SportCategory.STRENGTH, "腿部拉伸", 24f))
        add(SportItem(SportCategory.STRENGTH, "平板支撑", 50f))
        add(SportItem(SportCategory.STRENGTH, "背部拉伸", 20f))
        add(SportItem(SportCategory.STRENGTH, "腿部泡沫轴", 30f))
        add(SportItem(SportCategory.STRENGTH, "腿部泡沫轴放松", 30f))
        add(SportItem(SportCategory.STRENGTH, "哑铃划船", 58f))
        add(SportItem(SportCategory.STRENGTH, "仰卧起坐", 60f))
        add(SportItem(SportCategory.STRENGTH, "箭步蹲", 65f))
        add(SportItem(SportCategory.STRENGTH, "俄罗斯转体", 55f))
        add(SportItem(SportCategory.STRENGTH, "靠墙静蹲", 40f))
        add(SportItem(SportCategory.STRENGTH, "臀推", 65f))
        add(SportItem(SportCategory.STRENGTH, "卷腹", 55f))
        add(SportItem(SportCategory.STRENGTH, "俯卧撑", 132f))
        add(SportItem(SportCategory.STRENGTH, "引体向上", 73.3f))
        add(SportItem(SportCategory.STRENGTH, "深蹲", 60f))
        add(SportItem(SportCategory.STRENGTH, "硬拉", 77f))
        add(SportItem(SportCategory.STRENGTH, "卧推", 58f))
        add(SportItem(SportCategory.STRENGTH, "哑铃", 58f))
        add(SportItem(SportCategory.STRENGTH, "臀桥", 60f))
    }

    // ── 第二单位决策表 ────────────────────────────────────────────────

    /** km：位移型运动 → 10 分钟跑/骑多少公里 */
    private val KM_PER_10MIN = mapOf(
        // 跑步健走
        "跑步（快速）" to 2.0f, "跑步（中速）" to 1.67f, "跑步（慢速）" to 1.33f,
        "马拉松" to 1.67f, "走路" to 0.83f, "散步" to 0.67f, "步行" to 0.83f,
        "徒步快走" to 1.08f, "爬坡" to 1.0f,
        // 户外骑行
        "骑行" to 3.0f, "骑共享单车" to 2.5f, "骑山地车" to 2.33f, "户外骑行" to 2.67f
    )

    /** 局：一局多少分钟，没列出来的默认 20 分钟 */
    private val ROUND_MINUTES = mapOf(
        "乒乓球" to 10f, "台球" to 10f, "保龄球" to 10f, "拳击" to 10f,
        "羽毛球" to 15f, "壁球" to 15f, "短柄墙球" to 15f, "篮球（投篮）" to 15f,
        "足球" to 25f, "橄榄球" to 25f, "美式足球" to 25f, "板球" to 25f,
        "棒球" to 25f, "垒球" to 25f, "长曲棍球" to 25f, "冰上曲棍球" to 25f,
        "高尔夫" to 25f
    )

    /** 场：一场多少分钟，没列出来的默认 40 分钟 */
    private val SESSION_MINUTES = mapOf(
        "冥想" to 30f, "伸展运动" to 30f, "慢速舞蹈" to 30f, "健美操" to 30f,
        "瑜伽" to 45f, "力量瑜伽" to 45f, "空中瑜伽" to 45f, "高温瑜伽" to 45f,
        "普拉提" to 45f, "太极" to 45f, "广场舞" to 45f,
        "游泳" to 45f, "自由泳" to 45f, "蛙泳" to 45f, "仰泳" to 45f, "蝶泳" to 45f,
        "登山" to 50f, "爬山" to 50f, "耐力运动" to 50f, "赛艇" to 50f,
        "越野滑雪" to 50f, "滑旱冰" to 50f, "山地自行车" to 50f,
        "帆船" to 50f, "骑马" to 50f
    )

    /** 个：一分钟做多少个，没列出来的默认 20 个/分钟 */
    private val REPS_PER_MINUTE = mapOf(
        "引体向上" to 6f, "硬拉" to 8f,
        "卧推" to 10f, "杠铃卧推" to 10f, "平板卧推" to 10f,
        "站姿杠铃推举" to 10f, "哑铃肩推" to 10f, "壶铃甩" to 10f,
        "哑铃划船" to 12f, "哑铃深蹲" to 12f, "哑铃平底飞鸟" to 12f,
        "哑铃" to 15f, "弹力带臀桥" to 15f, "靠墙俯卧撑" to 15f,
        "卷腹" to 25f, "仰卧起坐" to 25f
    )

    /** 肌力训练里不走「个」的：拉伸、静力、循环、器械综合 → 按「场」算 */
    private val STRENGTH_SESSION = setOf(
        "肩颈拉伸", "腹部拉伸", "全身拉伸", "腿部拉伸", "背部拉伸",
        "腿部泡沫轴", "腿部泡沫轴放松", "平板支撑", "靠墙静蹲",
        "全身循环", "爬楼", "壶铃", "举重", "举重锻炼", "健美", "臂力棒", "呼啦圈"
    )

    /** 每个运动分到哪个第二单位 */
    private fun unitOf(item: SportItem): SportUnit = when {
        item.category == SportCategory.RUN || item.category == SportCategory.CYCLING -> SportUnit.KM
        item.category == SportCategory.BALL -> SportUnit.ROUND
        item.category == SportCategory.STRENGTH && item.name !in STRENGTH_SESSION -> SportUnit.REP
        else -> SportUnit.SESSION
    }

    /** 10 分钟里有几个第二单位 */
    private fun per10Of(item: SportItem, unit: SportUnit): Float = when (unit) {
        SportUnit.KM -> KM_PER_10MIN[item.name] ?: 1f
        SportUnit.ROUND -> 10f / (ROUND_MINUTES[item.name] ?: 20f)
        SportUnit.SESSION -> 10f / (SESSION_MINUTES[item.name] ?: 40f)
        SportUnit.REP -> (REPS_PER_MINUTE[item.name] ?: 20f) * 10f
        SportUnit.MIN -> 0f
    }

    /** 对外用的最终清单：单位与换算系数已经算好 */
    val items: List<SportItem> = rawItems.map { raw ->
        val unit = unitOf(raw)
        raw.copy(secondUnit = unit, secondUnitsPer10Min = per10Of(raw, unit))
    }

    /** 某个分类下的运动；「我的常用」「自定义」没有数据，用于展示空态 */
    fun byCategory(category: SportCategory): List<SportItem> =
        items.filter { it.category == category }
}

