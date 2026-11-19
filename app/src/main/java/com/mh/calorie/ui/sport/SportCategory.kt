package com.mh.calorie.ui.sport

import androidx.annotation.StringRes
import com.mh.calorie.R

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

/** 一条运动；kcal 是「每 10 分钟」的消耗，和截图上标的单位一致 */
data class SportItem(
    val category: SportCategory,
    val name: String,
    val kcalPer10Min: Float
)

/**
 * 静态运动库（UI 阶段数据，来源：设计稿运动库清单）。
 * 名称暂时写死中文：接 Room / 服务端时整体换成实体表，届时再迁到 strings。
 */
object SportCatalog {

    val items: List<SportItem> = buildList {
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

    /** 某个分类下的运动；「我的常用」「自定义」没有数据，用于展示空态 */
    fun byCategory(category: SportCategory): List<SportItem> =
        items.filter { it.category == category }
}