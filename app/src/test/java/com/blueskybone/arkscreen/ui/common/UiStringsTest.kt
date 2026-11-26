package com.blueskybone.arkscreen.ui.common

import com.blueskybone.arkscreen.R
import org.junit.Assert.assertEquals
import org.junit.Test

class UiStringsTest {
    @Test fun importSummaryFormatsBothCounts() {
        assertEquals("新增 12 条，跳过 3 条重复记录",
            TestUiStrings.get(R.string.gacha_import_deduplicated, 12, 3))
    }

    @Test fun importConfirmationPreservesWarningsAndLineBreaks() {
        val warning = TestUiStrings.get(R.string.gacha_import_source_warning)
        assertEquals("来源：账号 A\n目标：账号 B\n记录：2 条\n\n注意：备份来源与当前账号不同。",
            TestUiStrings.get(R.string.gacha_import_confirmation, "账号 A", "账号 B", 2, warning, ""))
    }

    @Test fun notificationPreservesAccountAndOperatorNames() {
        assertEquals("账号 A 阿米娅 可由艾丽妮换班",
            TestUiStrings.get(R.string.reminder_training_replace, "账号 A", "阿米娅", "艾丽妮"))
    }
}
