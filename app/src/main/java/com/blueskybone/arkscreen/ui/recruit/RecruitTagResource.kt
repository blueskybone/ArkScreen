package com.blueskybone.arkscreen.ui.recruit

object RecruitTagSource {

    val buttonList1 = listOf("高级资深干员", "资深干员", "新手")

    val buttonList2 = listOf(
        "近卫干员",
        "狙击干员",
        "重装干员",
        "医疗干员",
        "辅助干员",
        "术师干员",
        "特种干员",
        "先锋干员"
    )

    val buttonList3 = listOf("近战位", "远程位")

    val buttonList4 = listOf(
        "支援机械",
        "控场",
        "爆发",
        "治疗",
        "支援",
        "费用回复",
        "输出",
        "生存",
        "群攻",
        "防护",
        "减速",
        "削弱",
        "快速复活",
        "位移",
        "召唤",
        "元素"
    )

    const val TAG_MAX = 6

    fun buildAllTags(): List<TagItem> {
        val groups = listOf(buttonList1, buttonList2, buttonList3, buttonList4)
        return groups.flatMapIndexed { index, list ->
            list.map { TagItem(name = it, group = index) }
        }
    }
}