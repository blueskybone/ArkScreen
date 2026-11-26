package com.blueskybone.arkscreen.domain.model.recruit

import com.blueskybone.arkscreen.domain.model.recruit.AutoRecruitSession
import org.junit.Assert.*
import org.junit.Test

class AutoRecruitSessionTest {
    @Test fun comparesTagsInOrderAndCopiesInput() {
        val session = AutoRecruitSession()
        val tags = mutableListOf("输出", "减速")
        assertFalse(session.isDuplicate(tags))
        session.remember(tags)
        assertTrue(session.isDuplicate(listOf("输出", "减速")))
        assertFalse(session.isDuplicate(listOf("减速", "输出")))
        assertFalse(session.isDuplicate(listOf("输出", "治疗")))
        tags.clear()
        assertTrue(session.isDuplicate(listOf("输出", "减速")))
        assertFalse(AutoRecruitSession().isDuplicate(listOf("输出", "减速")))
    }
}
